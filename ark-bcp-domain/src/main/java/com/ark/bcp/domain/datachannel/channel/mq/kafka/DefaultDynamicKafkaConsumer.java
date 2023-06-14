

package com.ark.bcp.domain.datachannel.channel.mq.kafka;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.datachannel.channel.mq.base.AbstractMessageListenerConsumer;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.util.ConfigLoaderUtils;
import com.mryx.monitor.api.BusinessMonitor;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 */
public class DefaultDynamicKafkaConsumer
        extends AbstractMessageListenerConsumer
        implements MessageListener<String, Object> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDynamicKafkaConsumer.class);


    private static final String GROUP_ID = "risk-bcp";
    private String topic;
    private String bootstrapServers;

    private MessageListenerContainer container = null;

    public DefaultDynamicKafkaConsumer(String topic, String bootstrapServers) {
        this.topic = topic;
        this.bootstrapServers = bootstrapServers;
        Properties properties = properties();
        ContainerProperties containerProperties = containerProperties(properties);
        container = messageListenerContainer(containerProperties, properties);
    }

    private Properties properties() {
        Properties properties = null;
        try {
            properties = ConfigLoaderUtils.loadConfig("risk-kafka.properties");
            if (properties.isEmpty()) {
                return null;
            }
            return properties;
        } catch (Exception e) {
            logger.error("load /risk-kafka.properties 失败", e);
        }
        return null;
    }

    /**
     * 创建消费者参数信息.
     *
     * @param properties ""
     * @return ""
     */
    private Map<String, Object> consumerProperties(@Autowired Properties properties) {
        Map<String, Object> configMap = Maps.newHashMap();
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                properties.getProperty("kafka.consumer.enable.auto.commit"));
        configMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                properties.getProperty("kafka.consumer.session.timeout.ms"));
        configMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
                properties.getProperty("kafka.consumer.auto.commit.interval.ms"));
        configMap.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG,
                properties.getProperty("kafka.consumer.retry.backoff.ms"));
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                properties.getProperty("kafka.consumer.auto.reset"));
        return configMap;
    }

    public ContainerProperties containerProperties(Properties properties) {
        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setMessageListener(this);
        containerProperties.setAckMode(AbstractMessageListenerContainer.AckMode.RECORD);
        return containerProperties;
    }

    public MessageListenerContainer messageListenerContainer(
            ContainerProperties containerProperties,
            Properties properties
    ) {
        Map<String, Object> consumerProperies = consumerProperties(properties);
        ConsumerFactory<String, Object> factory = new DefaultKafkaConsumerFactory<>(consumerProperies);

        ConcurrentMessageListenerContainer<String, Object> container =
                new ConcurrentMessageListenerContainer<>(factory, containerProperties);
        container.setConcurrency(Integer.parseInt(properties.getProperty("kafka.consumer.concurrency")));
        return container;
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data the data to be processed.
     */
    @Override
    public void onMessage(ConsumerRecord<String, Object> data) {
        process(data);
    }

    @Override
    public void safeStart() {
        container.start();
    }

    @Override
    public void safeStop() {
        container.stop();
    }

    /**
     * 获取sentinel名称.
     *
     * @return ""
     */
    @Override
    public String getSentinelResourceName() {
        return GROUP_ID + ".kafka." + topic;

    }

    /**
     * 处理json消息.
     *
     * @param object@return ""
     */
    @Override
    public boolean processInSentinel(Object object) {
        if (!(object instanceof ConsumerRecord)) {
            logger.warn("processInSentinel error message list size");
            return true;
        }
        ConsumerRecord<String, Object> data = (ConsumerRecord) object;

        String body = (String) data.value();
        if (StringUtils.isEmpty(body)) {
            logger.info("processInSentinel error null messageext");
            return true;
        }

        MultiTagMonitor.record(MultiTagItem.build("busi-handle_kafka_message", "消费Kafka")
                .addTag("topic", data.topic()));
        EventMessageEntity<?> eventMsg = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            String messageId = StringUtils.isEmpty(data.key()) ? UUID.randomUUID().toString() : data.key();
            eventMsg = EventMessageEntity.builder()
                    .messageId(messageId)
                    .messageBody(JSON.parseObject(body))
                    .rawBody(body)
                    .receiveTime(new Date()).build();
        } catch (Exception e) {
            BusinessMonitor.recordOne("rocketmq-bodyex_" + topic);
            logger.info("将MQ消息转成EventMessage对象异常,{}", body, e);
        } finally {
            String topicAndTag = topic;
            BusinessMonitor.recordOne("busi-kafka-rt_" + topicAndTag, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
        onMesssage(eventMsg);
        return true;
    }
}
