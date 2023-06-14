

package com.ark.bcp.domain.engine.frame.decision.flow;

import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;

/**
 */
public interface DecisionFlowInterceptor {
    /**
     *  处理事件
     * @param eventSourceConfigEntity
     * @param messageEntity
     * @return true 消息被消费。false，消息未被消费
     * @throws Exception
     */
    boolean handle(
            final EventSourceConfigEntity eventSourceConfigEntity,
            final EventMessageEntity<?> messageEntity) throws Exception;
}
