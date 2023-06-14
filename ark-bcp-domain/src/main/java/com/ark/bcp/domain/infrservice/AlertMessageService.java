package com.ark.bcp.domain.infrservice;

import com.ark.bcp.domain.vo.AlertMessageValueObject;
import com.ark.bcp.domain.entity.BcpCheckRuleAlertEntity;

/**
 */
public interface AlertMessageService {

    /**
     * 发送消息
     *
     * @param alertValueObject
     * @param alertEntity
     */
    void sendMessage(AlertMessageValueObject alertValueObject, BcpCheckRuleAlertEntity alertEntity);
}
