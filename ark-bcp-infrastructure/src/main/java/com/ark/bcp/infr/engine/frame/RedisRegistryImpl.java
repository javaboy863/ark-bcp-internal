
package com.ark.bcp.infr.engine.frame;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.missfresh.risk.bcp.domain.engine.frame.rss.AbstractFailbackConfigChangeService;
import com.missfresh.risk.bcp.domain.engine.frame.rss.NotifyListener;
import com.missfresh.risk.bcp.domain.util.ConfigLoaderUtils;
import com.missfresh.risk.bcp.domain.util.Namespace;
import com.missfresh.risk.bcp.domain.util.NamedThreadFactory;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
@SuppressWarnings({"MethodLength", "LineLength"})
@Service(value = "redisRegistry")
public class RedisRegistryImpl extends AbstractFailbackConfigChangeService {

    private static final Logger logger = LoggerFactory.getLogger(RedisRegistryImpl.class);

    private static final String PROPERTY_FILE_NAME = "risk_engine_core_config_notifer.properties";

    private static final int DEFAULT_REDIS_PORT = 6379;

    private final Map<String, JedisPool> jedisPools = new ConcurrentHashMap<>();

    private Set<Notifier> notifiers = Sets.newConcurrentHashSet();


    private final ConcurrentMap<String, Map<NotifyListener, Object>> notifyListers = new ConcurrentHashMap<>();
    /**
     * 重连阈值
     */
    private int reconnectPeriod;

    private String channelName;

    public RedisRegistryImpl() {
        init();
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    private void init() {

        Properties params = null;
        try {
            params = ConfigLoaderUtils.loadConfig(PROPERTY_FILE_NAME);
            if (params.isEmpty()) {
                throw new RuntimeException(PROPERTY_FILE_NAME + " is not found");
            }
        } catch (Exception e) {
            throw new RuntimeException(PROPERTY_FILE_NAME + " is load error");
        }

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setTestOnBorrow(Boolean.parseBoolean(params.getProperty("test.on.borrow", "true")));
        config.setTestOnReturn(Boolean.parseBoolean(params.getProperty("test.on.return", "false")));
        config.setTestWhileIdle(Boolean.parseBoolean(params.getProperty("test.while.idle", "false")));

        int maxIdle = Integer.parseInt(params.getProperty("max.idle", "0"));
        if (maxIdle > 0) {
            config.setMaxIdle(maxIdle);
        }
        int minIdle = Integer.parseInt(params.getProperty("min.idle", "0"));
        if (minIdle > 0) {
            config.setMinIdle(minIdle);
        }
        int maxActive = Integer.parseInt(params.getProperty("max.active", "0"));
        if (maxActive > 0) {
            config.setMaxTotal(maxActive);
        }

        int maxTotal = Integer.parseInt(params.getProperty("max.total", "0"));
        if (maxTotal > 0) {
            config.setMaxTotal(maxTotal);
        }
        int timeout = Integer.parseInt(params.getProperty("timeout", "0"));
        int maxWait = Integer.parseInt(params.getProperty("max.wait", String.valueOf(timeout)));
        if (maxWait > 0) {
            config.setMaxWaitMillis(maxWait);
        }

        int numTestsPerEvictionRun = Integer.parseInt(params.getProperty("num.tests.per.eviction.run", "0"));
        if (numTestsPerEvictionRun > 0) {
            config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        }

        int timeBetweenEvictionRunsMillis = Integer.parseInt(params.getProperty("time.between.eviction.runs.millis", "0"));
        if (timeBetweenEvictionRunsMillis > 0) {
            config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        }

        int minEvictableIdleTimeMillis = Integer.parseInt(params.getProperty("min.evictable.idle.time.millis", "0"));
        if (minEvictableIdleTimeMillis > 0) {
            config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        }

        List<String> addresses = new ArrayList<String>();
        addresses.add(params.getProperty("address", ""));
        String backups = params.getProperty("address.backup", "");
        if (!StringUtils.isEmpty(backups)) {
            addresses.addAll(Arrays.asList(backups.split(",")));
        }

        for (String address : addresses) {
            int index = address.indexOf(':');
            String host;
            int port;
            if (index > 0) {
                host = address.substring(0, index);
                port = Integer.parseInt(address.substring(index + 1));
            } else {
                host = address;
                port = DEFAULT_REDIS_PORT;
            }
            this.jedisPools.put(address,
                    new JedisPool(
                            config, host, port,
                            Integer.parseInt(params.getProperty(Namespace.RegitserConstants.TIMEOUT_KEY, String.valueOf(Namespace.RegitserConstants.DEFAULT_TIMEOUT))),
                            params.getProperty("password", null),
                            Integer.parseInt(params.getProperty("db.index", String.valueOf(0)))));
        }

        this.reconnectPeriod = Integer.parseInt(params.getProperty(Namespace.RegitserConstants.REGISTRY_RECONNECT_PERIOD_KEY, String.valueOf(Namespace.RegitserConstants.DEFAULT_REGISTRY_RECONNECT_PERIOD)));

        channelName = params.getProperty("channel.name.prefix", "");
        if (StringUtils.isEmpty(channelName)) {
            throw new RuntimeException("channel.name.prefix miss in " + PROPERTY_FILE_NAME);
        }
        start();
    }

    private void start() {
        Notifier notifier = new Notifier(channelName);
        notifier.start();
    }


    @Override
    public boolean isAvailable() {
        for (JedisPool jedisPool : jedisPools.values()) {
            try {
                Jedis jedis = jedisPool.getResource();
                try {
                    if (jedis.isConnected()) {
                        // At least one single machine is available.
                        return true;
                    }
                } finally {
                    jedis.close();
                }
            } catch (Exception t) {
                // donothing
            }
        }
        return false;
    }

    @Override
    public void destroy() {

        try {
            for (Notifier notifier : notifiers) {
                notifier.shutdown();
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
            JedisPool jedisPool = entry.getValue();
            try {
                jedisPool.destroy();
            } catch (Throwable t) {
                logger.error("Failed to destroy the redis registry client. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
            }
        }
    }


    private void doNotify(String channel, String message) {
        if (StringUtils.isEmpty(message)
                || StringUtils.isEmpty(channel)
                || !channel.startsWith(this.channelName)
        ) {
            logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "非法消息:{},{}", channel, message);
            return;
        }
        JSONObject jsonObject = JSON.parseObject(message);
        String topic = jsonObject.getString("topic");
        String content = jsonObject.getString("msg");
        Map<NotifyListener, Object> listenerObjectMap = notifyListers.get(topic);
        if (listenerObjectMap != null) {
            synchronized (listenerObjectMap) {
                for (NotifyListener listener : listenerObjectMap.keySet()) {
                    try {
                        listener.onNotify(topic, content);
                    } catch (Exception e) {
                        logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "配置变更执行异常:{},{}", topic, content, e);
                    }
                }
            }
        }
    }

    /**
     * 发布配置变更.
     *
     * @param topic   ""
     * @param message ""
     */
    @Override
    public void doPublish(String topic, String message) {
        boolean success = false;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("topic", topic);
            jsonObject.put("msg", message);
            for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                JedisPool jedisPool = entry.getValue();
                try {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        String cmd = jsonObject.toJSONString();
                        logger.info("send commend to {}, what:{}", channelName, cmd);
                        Long publish = jedis.publish(channelName, cmd);
                        logger.info("send commend to {}, result:{}", channelName, publish);
                        if (publish > 0) {
                            // 只向其中一台redis发送消息
                            success = true;
                            break;
                        }
                    } finally {
                        logger.info("jedis close:{}", jedis);
                        jedis.close();
                    }
                } catch (Throwable t) { // Retry another server
                    logger.error("Failed to subscribe service from redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
                    // If you only have a single redis, you need to take a rest to avoid overtaking a lot of CPU resources
                    try {
                        Thread.sleep(reconnectPeriod);
                    } catch (InterruptedException e) {
                        logger.error("thread is interrupted", e);
                    }
                }
            }
            if (!success) {
                logger.error("message publish failed, no subscribe");
            }
        } catch (Throwable t) {
            logger.error("do publish error, cause={}", t.getMessage());
        }
    }


    /**
     * 设置订阅服务.
     *
     * @param topic    ""
     * @param listener ""
     */
    @Override
    public void subscribe(String topic, NotifyListener listener) {
        if (StringUtils.isEmpty(topic)
                || null == listener) {
            return;
        }

        Map<NotifyListener, Object> notiys = notifyListers.get(topic);
        if (null == notiys) {
            Map<NotifyListener, Object> theNewMap = Maps.newConcurrentMap();
            notiys = notifyListers.putIfAbsent(topic, theNewMap);
            if (null == notiys) {
                notiys = theNewMap;
            }
        }
        synchronized (notiys) {
            notiys.put(listener, new Object());
        }
    }

    /**
     * 取消订阅.
     *
     * @param topic    ""
     * @param listener ""
     */
    @Override
    public void unsubscribe(String topic, NotifyListener listener) {
        if (StringUtils.isEmpty(topic)
                || null == listener) {
            return;
        }

        Map<NotifyListener, Object> notiys = notifyListers.get(topic);
        if (null != notiys) {
            synchronized (notiys) {
                notiys.remove(listener);
            }
        }

    }


    private class NotifySub extends JedisPubSub {
        @Override
        public void onMessage(String key, String msg) {
            if (logger.isInfoEnabled()) {
                logger.info("redis event: " + key + " = " + msg);
            }
            if (StringUtils.isEmpty(msg)) {
                logger.info("redis onmessage is empty");
                return;
            }

            doNotify(key, msg);
        }

        @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
        @Override
        public void onPMessage(String pattern, String key, String msg) {
            onMessage(key, msg);
        }
    }

    @Data
    private class Notifier implements Runnable {

        private final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("RiskRedisSubscribe", true) {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = super.newThread(runnable);
                thread.setUncaughtExceptionHandler((thread1, exeception) -> {
                    threadPoolExecutor.execute(RedisRegistryImpl.Notifier.this);
                    logger.info("failover notifier");
                });
                return thread;
            }
        });

        private final String service;
        // 自旋保护
        private volatile int connectRandom;
        private final AtomicInteger connectSkip = new AtomicInteger();
        private final AtomicInteger connectSkiped = new AtomicInteger();
        private final Random random = new Random();

        private volatile Jedis jedis;
        private volatile boolean first = true;
        private volatile boolean running = true;

        private Notifier(String service) {
            this.service = service;
        }

        private void resetSkip() {
            connectSkip.set(0);
            connectSkiped.set(0);
            connectRandom = 0;
        }

        private boolean isSkip() {
            int skip = connectSkip.get(); // Growth of skipping times
            int saveTimes = 10;
            // If the number of skipping times increases by more than 10, take the random number
            if (skip >= saveTimes) {
                if (connectRandom == 0) {
                    connectRandom = random.nextInt(saveTimes);
                }
                skip = saveTimes + connectRandom;
            }
            if (connectSkiped.getAndIncrement() < skip) { // Check the number of skipping times
                return true;
            }
            connectSkip.incrementAndGet();
            connectSkiped.set(0);
            connectRandom = 0;
            return false;
        }

        public void start() {
            threadPoolExecutor.execute(Notifier.this);
        }


        public void shutdown() {
            try {
                threadPoolExecutor.shutdown();
                running = false;
                jedis.disconnect();
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }


        @Override
        public void run() {
            while (running) {
                if (!isSkip()) {
                    for (Map.Entry<String, JedisPool> entry : jedisPools.entrySet()) {
                        JedisPool jedisPool = entry.getValue();
                        try {
                            jedis = jedisPool.getResource();
                            try {
                                if (first) {
                                    first = false;
                                    resetSkip();
                                }
                                jedis.psubscribe(new NotifySub(), service); // blocking
                                break;
                            } finally {
                                jedis.close();
                            }
                        } catch (Throwable t) { // Retry another server
                            logger.error("Failed to subscribe service from redis registry. registry: " + entry.getKey() + ", cause: " + t.getMessage(), t);
                            // If you only have a single redis, you need to take a rest to avoid overtaking a lot of CPU resources
                            try {
                                Thread.sleep(reconnectPeriod);
                            } catch (InterruptedException e) {
                                logger.error("thread is interrupted", e);
                            }
                        }
                    }
                }
            }
        }

    }

}
