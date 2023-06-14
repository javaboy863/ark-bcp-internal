package com.ark.bcp.domain.datachannel;

import com.ark.bcp.domain.datachannel.channel.EventMessageListenner;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;

/**
 */
public abstract class BaseEventSource implements EventMessageListenner {
    /**
     * 消息监听者
     */
    private volatile EventMessageHandler eventMessageHandler;
    private final Object eventMessageLockObject = new Object();

    /**
     * 事件描述信息
     */
    private EventSourceConfigEntity eventSourceConfigEntity;

    public BaseEventSource(EventSourceConfigEntity eventSourceConfigEntity) {
        this.eventSourceConfigEntity = eventSourceConfigEntity;
    }

    public EventSourceConfigEntity getEventSourceConfigEntity() {
        return eventSourceConfigEntity;
    }

    public void setEventSourceConfigEntity(EventSourceConfigEntity eventSourceConfigEntity) {
        EventSourceConfigEntity oldEntity = this.eventSourceConfigEntity;
        this.eventSourceConfigEntity = eventSourceConfigEntity;
        onEventSourceConfigChange(oldEntity, this.eventSourceConfigEntity);
    }

    public void onEventSourceConfigChange(final EventSourceConfigEntity oldEntity, final EventSourceConfigEntity newEntity) {

    }

    /**
     * 收到消息通知
     *
     * @param eventMessage
     * @return
     */
    @Override
    public boolean onMesssage(EventMessageEntity<?> eventMessage) {
        if (eventMessage == null || null == eventMessageHandler) {
            return false;
        }
        eventMessage.setDataSourceId(getEventSourceConfigEntity().getId());
        return eventMessageHandler.handle(eventMessage);
    }

    public void setEventListner(EventMessageHandler handler) {
        synchronized (eventMessageLockObject) {
            this.eventMessageHandler = handler;
        }
    }

    /**
     * 初始化数据通道
     *
     * @return
     */
    public abstract boolean initDataChannel();
}
