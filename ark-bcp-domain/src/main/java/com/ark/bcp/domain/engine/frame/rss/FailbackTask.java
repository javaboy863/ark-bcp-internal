
package com.ark.bcp.domain.engine.frame.rss;

/**
 */
public interface FailbackTask {

    /**
     * 一致性检查
     */
    void onFailback();

    /**
     * 释放资源
     */
    void release();

}
