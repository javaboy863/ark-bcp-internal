

package com.ark.bcp.domain.datachannel.factories.listen;

import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.event.lisner.KafkaEventSource;
import com.ark.bcp.domain.datachannel.factories.IEventSourceFactory;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class KafkaListenEventSourceFactory implements IEventSourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(KafkaListenEventSourceFactory.class);

    @Override
    public BaseEventSource createEventSource(final EventSourceConfigEntity eventSourceConfigEntity) {
        if (null == eventSourceConfigEntity) {
            return null;
        }
        try {
            BaseEventSource eventSource = new KafkaEventSource(eventSourceConfigEntity);
            logger.info("创建Kafka事件:{}", eventSourceConfigEntity.getId());
            return eventSource;
        } catch (Exception e) {
            logger.error("创建kafka事件异常:{}",eventSourceConfigEntity.getId(), e);
        }
        return null;
    }
}
