

package com.ark.bcp.domain.engine.frame.rss;


/**
 */
public interface ConfigChangeNotifier {

    /**
     * 发布配置变更.
     *
     * @param topic   ""
     * @param message ""
     */
    void doPublish(String topic, String message);

    /**
     * 设置订阅服务.
     *
     * @param topic    ""
     * @param listener ""
     */
    void subscribe(String topic, NotifyListener listener);

    /**
     * 取消订阅.
     *
     * @param topic    ""
     * @param listener ""
     */
    void unsubscribe(String topic, NotifyListener listener);

    /**
     * 注册一致性检查
     *
     * @param key
     * @param failbackTask
     * @return
     */
    boolean doRegistFallbackTask(String key, FailbackTask failbackTask);

    /**
     * 反注册一致性检查
     *
     * @param key
     * @param failbackTask
     */
    void unRegistFallbackTask(String key, FailbackTask failbackTask);

    /**
     * 可用性标记
     *
     * @return
     */
    boolean isAvailable();

    /**
     * 销毁
     */
    void destroy();
}
