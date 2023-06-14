

package com.ark.bcp.domain.engine.frame.decision.flow.handles;

import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.infrservice.MessagePoolService;
import com.ark.bcp.domain.engine.frame.decision.flow.DecisionFlowInterceptor;
import com.ark.bcp.domain.util.EventSourceConfigEntityUtils;
import com.ark.bcp.domain.util.MapContextFormator;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 */
@Component
@Order(20)
public class MessagePoolHandleInterceptor implements DecisionFlowInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MessagePoolHandleInterceptor.class);

    @Resource
    private MessagePoolService messagePoolService;


    @Override
    public boolean handle(
            @NonNull final EventSourceConfigEntity eventSourceConfigEntity,
            @NonNull final EventMessageEntity<?> messageEntity) throws Exception {

        if (null != eventSourceConfigEntity.getMatchTemplateEntity()
                && 0 != eventSourceConfigEntity.getMatchTemplateEntity().getSaveToMatchDbFlag()
                && !StringUtils.isEmpty(eventSourceConfigEntity.getMatchTemplateEntity().getTemplateName())) {
            String messageKey = MapContextFormator.formatRealTempleteKey(
                    eventSourceConfigEntity.getMatchTemplateEntity().getTemplateName(),
                    JSONObject.parseObject(messageEntity.getRawBody()));
            messagePoolService.saveEventMessageToPool(messageKey, messageEntity);
            logger.info("event is message pool:{},{}", eventSourceConfigEntity.getId(), messageEntity.getMessageId());
            MultiTagMonitor.record(MultiTagItem.build(
                    EventSourceConfigEntityUtils.decisionMonitorId(eventSourceConfigEntity),
                    EventSourceConfigEntityUtils.decisionMonitorName(eventSourceConfigEntity)).addTag("reason", "pooled"));
            return true;
        }
        return false;
    }
}
