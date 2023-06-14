
package com.ark.bcp.domain.engine.frame.rss;

/**
 */
public interface NotifyListener {

    /**
     * 通知接口
     *
     * @param topic
     * @param serializable
     */
    void onNotify(String topic, String serializable);
}
