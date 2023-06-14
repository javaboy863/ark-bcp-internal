
package com.ark.bcp.domain.infrservice;

import com.ark.bcp.domain.entity.EventMessageEntity;

/**
 */
public interface MessagePoolService {
    /**
     * 保存配对消息
     * @param matchKey
     * @param messageEntity
     */
    void saveEventMessageToPool(String matchKey, EventMessageEntity<?> messageEntity);

    /**
     * 获取配对消息
     * @param matchKey
     * @return
     */
    EventMessageEntity<?> readEventMessageFromPool(String matchKey);
}
