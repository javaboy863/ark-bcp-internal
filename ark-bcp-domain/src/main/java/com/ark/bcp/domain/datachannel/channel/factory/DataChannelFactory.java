

package com.ark.bcp.domain.datachannel.channel.factory;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.datachannel.channel.mq.rocketmq.DefaultDynamicRocketMqConsumer;
import com.ark.bcp.domain.datachannel.channel.mq.kafka.DefaultDynamicKafkaConsumer;
import com.ark.bcp.domain.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class DataChannelFactory {
    private static final Logger logger = LoggerFactory.getLogger(DataChannelFactory.class);
    private static ConcurrentHashMap<KafkaConnParam, DefaultDynamicKafkaConsumer> kafkaConsumers = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<RocketMqConnParam, DefaultDynamicRocketMqConsumer> mqConsumers = new ConcurrentHashMap<>();


    public static DefaultDynamicKafkaConsumer createDefaultDynamicKafkaConsumer(String params) {
        logger.info("启动参数:{}", params);

        KafkaConnParam kafkaConnParam = JSON.parseObject(params, KafkaConnParam.class);
        if (StringUtils.isEmpty(kafkaConnParam.bootstrapServers) || StringUtils.isEmpty(kafkaConnParam.topic)) {
            return null;
        }
        DefaultDynamicKafkaConsumer consumer = null;
        if (kafkaConsumers.containsKey(kafkaConnParam)) {
            consumer = kafkaConsumers.get(kafkaConnParam);
        } else {
            consumer = new DefaultDynamicKafkaConsumer(
                    kafkaConnParam.getTopic(),
                    kafkaConnParam.getBootstrapServers());
            kafkaConsumers.putIfAbsent(kafkaConnParam, consumer);
        }
        return consumer;
    }

    public static DefaultDynamicRocketMqConsumer createBaseCosumer(final String params) {
        logger.info("启动参数:{}", params);
        RocketMqConnParam mqConnParam = parseRocketMqConnParam(params);
        if (StringUtils.isEmpty(mqConnParam.getNameserver())
                || StringUtils.isEmpty(mqConnParam.getTopic())) {
            return null;
        }
        DefaultDynamicRocketMqConsumer mqConsumer = null;
        if (mqConsumers.containsKey(mqConnParam)) {
            mqConsumer = mqConsumers.get(mqConnParam);
        } else {
            mqConsumer = new DefaultDynamicRocketMqConsumer(
                    mqConnParam.getTopic(),
                    StringUtils.isEmpty(mqConnParam.getConsumerGroupName()) ? Constant.BCP_MQ_CONSUMER_GEOUP : mqConnParam.getConsumerGroupName(),
                    mqConnParam.getNameserver());
            mqConsumers.putIfAbsent(mqConnParam, mqConsumer);
        }
        return mqConsumer;
    }

    public static RocketMqConnParam parseRocketMqConnParam(String params) {
        return JSON.parseObject(params, RocketMqConnParam.class);
    }

    public static class KafkaConnParam {
        private String bootstrapServers;
        private String topic;

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KafkaConnParam that = (KafkaConnParam) o;
            // topic 必须相同
            // that.bootstrapServers 是 this.bootstrapServers 的子集
            return Objects.equals(topic, that.topic) && this.bootstrapServers.contains(that.bootstrapServers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bootstrapServers, topic);
        }
    }


    public static class RocketMqConnParam {
        private String topic;
        private String nameserver;
        private String tag;
        private String consumerGroupName;

        public String getConsumerGroupName() {
            return consumerGroupName;
        }

        public void setConsumerGroupName(String consumerGroupName) {
            this.consumerGroupName = consumerGroupName;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getNameserver() {
            return nameserver;
        }

        public void setNameserver(String nameserver) {
            this.nameserver = nameserver;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RocketMqConnParam that = (RocketMqConnParam) o;
            return Objects.equals(topic, that.topic) &&
                    Objects.equals(consumerGroupName, that.consumerGroupName) &&
                    Objects.equals(nameserver, that.nameserver);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topic, consumerGroupName, nameserver);
        }
    }

}
