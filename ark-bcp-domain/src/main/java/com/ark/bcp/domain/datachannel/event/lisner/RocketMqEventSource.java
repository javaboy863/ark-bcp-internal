package com.ark.bcp.domain.datachannel.event.lisner;

import com.ark.bcp.domain.annotations.Reentrant;
import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.DynamicDataSource;
import com.ark.bcp.domain.datachannel.channel.factory.DataChannelFactory;
import com.ark.bcp.domain.datachannel.channel.DataChannel;
import com.ark.bcp.domain.datachannel.channel.mq.rocketmq.DefaultDynamicRocketMqConsumer;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.util.Constant;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.filter.FilterAPI;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;

/**
 */
@Slf4j
public class RocketMqEventSource extends BaseEventSource implements DynamicDataSource {

    private volatile DataChannelFactory.RocketMqConnParam mqConnParam = null;
    private volatile DefaultDynamicRocketMqConsumer mqConsumer = null;

    public RocketMqEventSource(EventSourceConfigEntity eventSourceConfigEntity) {
        super(eventSourceConfigEntity);
    }

    @Override
    @Reentrant
    public boolean initDataChannel() {
        DefaultDynamicRocketMqConsumer oldConsumer = mqConsumer;

        mqConnParam = DataChannelFactory.parseRocketMqConnParam(getEventSourceConfigEntity().getDetailConf());
        mqConsumer = DataChannelFactory.createBaseCosumer(getEventSourceConfigEntity().getDetailConf());
        // 如果原样的consumenr，不需要处理
        if (oldConsumer == mqConsumer) {
            return true;
        }

        if (oldConsumer != null) {
            oldConsumer.dettachEventMessageLisener(this);
        }
        if (null != mqConsumer) {
            ((DataChannel) mqConsumer).attachEventMessageListener(this);
            mqConsumer.start();
            return true;
        } else {
            log.info("创建事件失败");
            return false;
        }
    }

    /**
     * 接收到事件消息的处理，采样、通知订阅方等
     *
     * @param eventMsg
     * @return
     */
    @Override
    public boolean onMesssage(EventMessageEntity<?> eventMsg) {
        if (!(eventMsg.getBizObject() instanceof MessageExt)) {
            log.info("丢弃消息:{}", eventMsg);
            return true;
        }
        String tag = mqConnParam.getTag();
        if (SubscriptionData.SUB_ALL.equalsIgnoreCase(tag)) {
            return doEvent(eventMsg);
        }

        MessageExt messageExt = (MessageExt) eventMsg.getBizObject();
        String messageTags = messageExt.getProperties().get("TAGS");
        SubscriptionData subscriptionData = null;
        try {
            subscriptionData = FilterAPI.buildSubscriptionData(Constant.BCP_MQ_CONSUMER_GEOUP, mqConnParam.getTopic(), messageTags);
        } catch (Exception e) {
            log.info("获取subscriptionData异常");
            return true;
        }
        if (subscriptionData.getTagsSet().contains(tag)) {
            return doEvent(eventMsg);
        }
        log.info("丢弃消息:{}", eventMsg.getMessageId());
        return true;

    }

    private boolean doEvent(EventMessageEntity<?> eventMsg) {
        try {
            eventMsg.setDataSourceId(getEventSourceConfigEntity().getId());
            return super.onMesssage(eventMsg);
        } finally {
            if (null != mqConnParam) {
                String topicAndTag = mqConnParam.getTopic() + "_" + mqConnParam.getTag();
                topicAndTag = topicAndTag.replace("*", "X");
                MultiTagMonitor.record(MultiTagItem.build("busi-handle_rocketmq_message", "消费RocketMQ")
                        .addTag("topic", topicAndTag));
            }
        }
    }

    @Override
    public boolean start() {
        if (null != mqConsumer) {
            mqConsumer.start();
        }
        return false;
    }

    @Override
    public boolean stop() {
        if (null != mqConsumer) {
            mqConsumer.stop();
        }
        return false;
    }
}
