

package com.ark.bcp.domain.alert;

import com.ark.bcp.domain.entity.BcpAlertConfigEntity;

/**
 */
public interface AlertFrequencyLimit {

    /**
     * 是否被流量控制
     *
     * @param alertConfigEntity
     * @return
     */
    boolean isLimited(BcpAlertConfigEntity alertConfigEntity);

    /**
     * 流量打点
     *
     * @param alertConfigEntity
     */
    void addTick(BcpAlertConfigEntity alertConfigEntity);
}
