package com.ark.bcp.domain.datachannel.event.lisner;

import com.ark.bcp.domain.annotations.Reentrant;
import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.DynamicDataSource;
import com.ark.bcp.domain.datachannel.channel.factory.DataChannelFactory;
import com.ark.bcp.domain.datachannel.channel.mq.kafka.DefaultDynamicKafkaConsumer;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
public class KafkaEventSource extends BaseEventSource implements DynamicDataSource {

    private volatile DefaultDynamicKafkaConsumer consumer = null;

    public KafkaEventSource(EventSourceConfigEntity eventSourceConfigEntity) {
        super(eventSourceConfigEntity);
    }

    @Reentrant
    @Override
    public boolean initDataChannel() {
        log.info("启动参数:{}", getEventSourceConfigEntity().getDetailConf());
        DefaultDynamicKafkaConsumer oldConsuemr = consumer;

        consumer = DataChannelFactory.createDefaultDynamicKafkaConsumer(getEventSourceConfigEntity().getDetailConf());
        // 如果原样的consumenr，不需要处理
        if (oldConsuemr == consumer) {
            return true;
        }

        if (null != oldConsuemr) {
            oldConsuemr.dettachEventMessageLisener(this);
        }

        if (consumer != null) {
            consumer.attachEventMessageListener(this);
            consumer.start();
            // 启动监听
            return true;
        } else {
            log.info("创建事件失败");
            return false;
        }
    }

    @Override
    public boolean start() {
        if (null != consumer) {
            consumer.start();
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (null != consumer) {
            consumer.stop();
        }
        return true;
    }
}
