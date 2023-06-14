

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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 */
@Component
@Order(10)
public class SampleHandleInterceptor implements DecisionFlowInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(SampleHandleInterceptor.class);

    private static final int MAX_PERSENT = 100;
    private static final int MIN_PERSENT = 0;

    @Override
    public boolean handle(
            @NonNull final EventSourceConfigEntity eventSourceConfigEntity,
            @NonNull final EventMessageEntity<?> messageEntity) throws Exception {

        if (!isSampleHandle(eventSourceConfigEntity)) {
            logger.info("event is sample handled:{},{}", eventSourceConfigEntity.getId(), messageEntity.getMessageId());
            MultiTagMonitor.record(MultiTagItem.build(
                    EventSourceConfigEntityUtils.decisionMonitorId(eventSourceConfigEntity),
                    EventSourceConfigEntityUtils.decisionMonitorName(eventSourceConfigEntity)).addTag("reason", "droped"));
            return true;
        }
        return false;
    }

    private static boolean isSampleHandle(EventSourceConfigEntity eventSourceConfigEntity) {
        if (null == eventSourceConfigEntity.getSampleRatio() || MAX_PERSENT <= eventSourceConfigEntity.getSampleRatio()) {
            return true;
        }
        Random random = new Random();
        int randomInt = random.nextInt(MAX_PERSENT);
        return randomInt <= eventSourceConfigEntity.getSampleRatio();
    }

}
