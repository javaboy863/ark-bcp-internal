

package com.ark.bcp.domain.datachannel.channel.mq.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.ark.bcp.domain.datachannel.channel.mq.base.AbstractMessageListenerConsumer;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.util.Constant;
import com.missfresh.rocketMQClient.consumer.BaseConsumer;
import com.mryx.monitor.api.BusinessMonitor;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 */
public class DefaultDynamicRocketMqConsumer extends AbstractMessageListenerConsumer {
    private static Logger logger = LoggerFactory.getLogger(DefaultDynamicRocketMqConsumer.class);

    private final String topic;
    private final String nameserver;
    private final String consumerGroup;

    private BaseConsumer baseConsumer;


    /**
     * 构造函数.
     *
     * @param topic      ""
     * @param nameserver ""
     */
    public DefaultDynamicRocketMqConsumer(String topic, String consumerGroup, String nameserver) {
        this.topic = topic;
        this.nameserver = nameserver;
        this.consumerGroup = consumerGroup;
    }

    @Override
    public void safeStart() {
        if (StringUtils.isEmpty(nameserver)
                || StringUtils.isEmpty(consumerGroup)
                || StringUtils.isEmpty(topic)) {
            return;
        }
        baseConsumer = new BaseConsumer() {
            @Override
            public boolean process(JSONObject jsonObject) {
                DefaultDynamicRocketMqConsumer.this.process(jsonObject);
                return true;
            }

            @Override
            public boolean process(List<MessageExt> list) {
                if (CollectionUtils.isEmpty(list) || list.size() != 1) {
                    logger.warn("processInSentinel error message list size:{}", list.size());
                    return true;
                }
                MultiTagMonitor.record(MultiTagItem.build("busi-consuemr_rocketmq", "消费RocketMQ")
                        .addTag("topic", topic));
                MessageExt messageExt = list.get(0);
                return DefaultDynamicRocketMqConsumer.this.process(messageExt);
            }

            @Override
            public void retryErrCallBack(String s, String s1, String s2, String s3, MessageExt messageExt) {
                logger.error("事件消息重复消费失败，事件源ID:{}，topic:{}, msgId:{}, messageBody:{}", topic, messageExt.getMsgId(), new String(messageExt.getBody()));
                BusinessMonitor.recordOne("rocketmq-dlq_" + topic);
            }
        };
        baseConsumer.setInstanceName(UUID.randomUUID().toString());
        int threadCnt = Runtime.getRuntime().availableProcessors() + 1;
        // messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h 6h 12h
        // 消费异常时重试消费次数,如果配置流控，那么需要选择合理的重试次数，防止流控过多导致死信陡增
        baseConsumer.setConsumerRetry(3);
        // 消费最小线程数
        baseConsumer.setConsumeThreadMin(threadCnt);
        // 消费最大线程数 // 由于底层使用的无界队列，所以这里最大最小需要设置成相同的值
        baseConsumer.setConsumeThreadMax(threadCnt);
        //默认批次量
        baseConsumer.setConsumeMessageBatchMaxSize(1);
        baseConsumer.startConsume(
                nameserver,
                consumerGroup,
                topic,
                SubscriptionData.SUB_ALL,
                ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET,
                false
        );
        logger.info("consumer has started:{},{}", nameserver, topic);
    }

    @Override
    public void safeStop() {
        if (null != baseConsumer) {
            baseConsumer.shutdown();
        }
    }

    /**
     * 获取sentinel名称.
     *
     * @return ""
     */
    @Override
    public String getSentinelResourceName() {
        return Constant.BCP_MQ_CONSUMER_GEOUP + ".rocketmq." + topic;
    }

    /**
     * 处理json消息.
     *
     * @param object ""
     * @return ""
     */
    @Override
    public boolean processInSentinel(Object object) {
        logger.info("topic {}", topic);
        MessageExt messageExt = null;
        if (object instanceof MessageExt) {
            messageExt = (MessageExt) object;
        } else {
            logger.info("processInSentinel error null messageext");
            return true;
        }

        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(body)) {
            logger.info("processInSentinel error empty body");
            return true;
        }
        EventMessageEntity<?> eventMsg = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            eventMsg = EventMessageEntity.builder()
                    .messageId(messageExt.getMsgId())
                    .messageBody(JSON.parseObject(body))
                    .rawBody(body)
                    .bizObject(messageExt)
                    .receiveTime(new Date()).build();
            return onMesssage(eventMsg);
        } catch (Exception e) {
            BusinessMonitor.recordOne("rocketmq-bodyex_" + topic);
            logger.info("将MQ消息转成EventMessage对象异常,{}", messageExt.getMsgId(), e);
            return true;
        } finally {
            BusinessMonitor.recordOne("busi-rocketmq-rt_" + topic, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }
}
