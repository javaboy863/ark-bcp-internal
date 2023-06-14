

package com.ark.bcp.domain.datachannel.channel;


import com.ark.bcp.domain.entity.EventMessageEntity;

/**
 */
public interface EventMessageListenner {
    /**
     * 收到消息通知
     *
     * @param eventMessage
     * @return
     */
    boolean onMesssage(final EventMessageEntity<?> eventMessage);
}
