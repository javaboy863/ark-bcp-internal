

package com.ark.bcp.domain.datachannel.channel;

/**
 */
public interface DataChannel {
    /**
     * 初始化通道
     *
     * @param params
     * @return
     */
    default boolean init(String params) {
        return false;
    }

    /**
     * 启动管道
     */
    default void start() {
    }

    /**
     * 关闭管道
     */
    default void stop() {
    }

    /**
     * 订阅消息
     *
     * @param listener
     */
    default void attachEventMessageListener(EventMessageListenner listener) {
    }

    /**
     * 取消订阅消息
     *
     * @param listener
     */
    default void dettachEventMessageLisener(EventMessageListenner listener) {
    }
}
