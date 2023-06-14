

package com.ark.bcp.domain.datachannel.factories.listen;

import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.event.lisner.RocketMqEventSource;
import com.ark.bcp.domain.datachannel.factories.IEventSourceFactory;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class RocketMqListenEventSourceFactory implements IEventSourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(RocketMqListenEventSourceFactory.class);

    @Override
    public BaseEventSource createEventSource(final EventSourceConfigEntity eventSourceConfigEntity) {
        if (null == eventSourceConfigEntity) {
            return null;
        }
        try {
            BaseEventSource eventSource = new RocketMqEventSource(eventSourceConfigEntity);
            logger.info("创建 rocketmq 事件:{}", eventSourceConfigEntity.getId());
            return eventSource;
        } catch (Exception e) {
            logger.error("创建 rocketmq 事件异常:{}", eventSourceConfigEntity.getId(),e);
        }
        return null;
    }
}
