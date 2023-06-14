

package com.ark.bcp.domain.datachannel.channel.mq.base;

import com.google.common.collect.Maps;
import com.ark.bcp.domain.datachannel.channel.DataChannel;
import com.ark.bcp.domain.datachannel.channel.EventMessageListenner;
import com.ark.bcp.domain.entity.EventMessageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 */
public abstract class AbstractMessageListenerConsumer
        extends AbstractSentinelMdcConsumer
        implements DataChannel, EventMessageListenner {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageListenerConsumer.class);
    private final Map<EventMessageListenner,Object> eventMessageListeners = Maps.newConcurrentMap();

    @Override
    public boolean onMesssage(EventMessageEntity<?> eventMessage) {
        for (Map.Entry<EventMessageListenner, Object> entry : eventMessageListeners.entrySet()) {
            entry.getKey().onMesssage(eventMessage);
        }
        return true;
    }

    /**
     * 订阅消息
     *
     * @param listener
     */
    @Override
    public void attachEventMessageListener(EventMessageListenner listener) {
        eventMessageListeners.put(listener, new Object());
        if (eventMessageListeners.size() > 1) {
            logger.info("event message listener > 1");
        }
    }

    /**
     * 取消订阅消息
     *
     * @param listener
     */
    @Override
    public void dettachEventMessageLisener(EventMessageListenner listener) {
        eventMessageListeners.remove(listener);
    }

    /**
     * 关闭管道
     */
    @Override
    final public void stop() {
        if (CollectionUtils.isEmpty(eventMessageListeners)) {
            safeStop();
        }
    }
}
