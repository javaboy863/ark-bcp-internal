package com.ark.bcp.domain.datachannel.factories;

import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;

/**
 */
public interface IEventSourceFactory {
    /**
     * 创建事件源
     *
     * @param eventSourceConfigEntity
     * @return
     */
    BaseEventSource createEventSource(EventSourceConfigEntity eventSourceConfigEntity);
}
