package com.ark.bcp.domain.engine.frame.rss;


import com.google.common.collect.Maps;
import com.mryx.sentinel.concurrent.NamedThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;


/**
 *
 */
public abstract class AbstractFailbackConfigChangeService implements ConfigChangeNotifier {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFailbackConfigChangeService.class);
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("RegistryFailedRetryTimer", true));

    private final ConcurrentMap<String, Map<FailbackTask, Object>> failbackTaskMap = new ConcurrentHashMap<>();


    public AbstractFailbackConfigChangeService() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                failbackTaskMap.entrySet().forEach(new Consumer<Map.Entry<String, Map<FailbackTask, Object>>>() {
                    @Override
                    public void accept(Map.Entry<String, Map<FailbackTask, Object>> stringMapEntry) {
                        if (CollectionUtils.isEmpty(stringMapEntry.getValue())) {
                            return;
                        }
                        stringMapEntry.getValue().entrySet().forEach(new Consumer<Map.Entry<FailbackTask, Object>>() {
                            @Override
                            public void accept(Map.Entry<FailbackTask, Object> failbackTaskObjectEntry) {
                                failbackTaskObjectEntry.getKey().onFailback();
                            }
                        });
                    }
                });
            }
        };
        retryExecutor.scheduleAtFixedRate(runnable, 0, 30, TimeUnit.SECONDS);

    }

    @Override
    public final boolean doRegistFallbackTask(String key, FailbackTask failbackTask) {
        if (StringUtils.isEmpty(key) || null == failbackTask) {
            return false;
        }
        Map<FailbackTask, Object> failbackTaskObjectMap = failbackTaskMap.get(key);
        if (null == failbackTaskObjectMap) {
            Map<FailbackTask, Object> theNewMap = Maps.newConcurrentMap();
            failbackTaskObjectMap = failbackTaskMap.putIfAbsent(key, theNewMap);
            if (null == failbackTaskObjectMap) {
                failbackTaskObjectMap = theNewMap;
            }
        }
        synchronized (failbackTaskObjectMap) {
            failbackTaskObjectMap.put(failbackTask, new Object());
        }
        return true;
    }

    @Override
    public void unRegistFallbackTask(String key, FailbackTask failbackTask) {
        if (StringUtils.isEmpty(key) || !failbackTaskMap.containsKey(key)) {
            return;
        }
        Map<FailbackTask, Object> failbackTaskObjectMap = failbackTaskMap.get(key);
        if (null != failbackTaskObjectMap) {
            Object obj = failbackTaskObjectMap.remove(failbackTask);
            logger.info("解除监听 {} 结果 {}", failbackTask, null == obj);
            if (null != obj) {
                failbackTask.release();
            }
        }
    }
}
