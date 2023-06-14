

package com.ark.bcp.domain.engine.frame.decision.flow.handles;

import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.engine.frame.decision.flow.DecisionFlowInterceptor;
import com.ark.bcp.domain.util.EventSourceConfigEntityUtils;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 */
@Component
@Order(0)
public class StatusInterceptor implements DecisionFlowInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(StatusInterceptor.class);

    @Override
    public boolean handle(
            final EventSourceConfigEntity eventSourceConfigEntity,
            final EventMessageEntity<?> messageEntity) throws Exception {
        if (null == eventSourceConfigEntity || null == messageEntity) {
            return true;
        }
        if (!eventSourceConfigEntity.isEnable()) {
            logger.info("event is closed:{},{}", eventSourceConfigEntity.getId(), messageEntity.getMessageId());
            MultiTagMonitor.record(MultiTagItem.build(
                    EventSourceConfigEntityUtils.decisionMonitorId(eventSourceConfigEntity),
                    EventSourceConfigEntityUtils.decisionMonitorName(eventSourceConfigEntity)).addTag("reason", "closed"));
            return true;
        }
        return false;
    }
}
