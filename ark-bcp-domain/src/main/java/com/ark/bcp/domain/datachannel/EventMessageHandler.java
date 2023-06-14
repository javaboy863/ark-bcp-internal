

package com.ark.bcp.domain.datachannel;


import com.ark.bcp.domain.entity.EventMessageEntity;

/**
 */
public interface EventMessageHandler {
    /**
     * 收到消息通知
     *
     * @param eventMessage
     * @return
     */
    boolean handle(final EventMessageEntity<?> eventMessage);
}
