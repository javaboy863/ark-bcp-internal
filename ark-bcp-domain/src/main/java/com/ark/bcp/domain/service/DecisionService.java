
package com.ark.bcp.domain.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.DecisionExcuteResult;
import com.ark.bcp.domain.engine.frame.decision.execute.FieldSetterReader;
import com.ark.bcp.domain.engine.frame.decision.execute.IDecisionContext;
import com.ark.bcp.domain.engine.frame.decision.execute.RuleExcuteResult;
import com.ark.bcp.domain.entity.BcpCheckRuleAlertEntity;
import com.ark.bcp.domain.entity.CheckFailRecordEntity;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.repository.riskbcp.BcpCheckRuleAlertConfigRepository;
import com.ark.bcp.domain.vo.AlertMessageValueObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.*;
import com.ark.bcp.domain.engine.frame.decision.flow.DecisonFlowHandlerExecutionChain;
import com.ark.bcp.domain.engine.frame.decision.flow.handles.MessageDelayHandleInterceptor;
import com.ark.bcp.domain.engine.frame.decision.flow.handles.MessagePoolHandleInterceptor;
import com.ark.bcp.domain.engine.frame.decision.flow.handles.SampleHandleInterceptor;
import com.ark.bcp.domain.engine.frame.decision.flow.handles.StatusInterceptor;
import com.missfresh.risk.bcp.domain.entity.*;
import com.ark.bcp.domain.util.AsyncUtil;
import com.ark.bcp.domain.util.EventSourceConfigEntityUtils;
import com.ark.bcp.domain.util.SentinelUtil;
import com.missfresh.risk.bcp.enums.AlertChannelDefine;
import com.mryx.monitor.api.BusinessMonitor;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 */
@Service
public class DecisionService implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(DecisionService.class);

    @Resource
    private StatusInterceptor statusInterceptor;

    @Resource
    private SampleHandleInterceptor sampleHandleInterceptor;

    @Resource
    private MessageDelayHandleInterceptor messageDelayHandleInterceptor;

    @Resource
    private MessagePoolHandleInterceptor messagePoolHandleInterceptor;

    @Resource
    private EventSourceConfigDomainService eventSourceConfigDomainService;

    @Resource
    private CheckRuleStatService checkRuleStatService;

    @Resource
    private CheckFailRecordService checkFailRecordService;

    @Resource
    private IDecisionContext decisionContext;

    @Resource
    private AlertMessageDomainService alertMessageDomainService;

    @Resource
    private RuleConfigDomainService ruleConfigDomainService;

    @Resource
    private BcpCheckRuleAlertConfigRepository alertConfigRepository;

    private final DecisonFlowHandlerExecutionChain executionChain = new DecisonFlowHandlerExecutionChain();
    private final DecisonFlowHandlerExecutionChain executionAsyncChain = new DecisonFlowHandlerExecutionChain();

    private BcpCheckRuleAlertEntity getRuleAlertChannelConfig(Long ruleid) {
        List<BcpCheckRuleAlertEntity> alertEntities = alertConfigRepository.selectBcpCheckRuleAlertConfigList(
                BcpCheckRuleAlertEntity.builder().ruleId(ruleid).build());
        if (CollectionUtils.isEmpty(alertEntities)) {
            return BcpCheckRuleAlertEntity.builder()
                    .ruleId(ruleid).alertType(AlertChannelDefine.ALERT_LARK_GROUP.getCode()).build();
        } else {
            return alertEntities.get(0);
        }
    }

    private void sendAlertMessage(final EventMessageEntity eventMessage, final DecisionExcuteResult decisionExcuteResult) {
        logger.info("decisionExcuteResult={}", JSONObject.toJSONString(decisionExcuteResult));
        try {
            if (!DecisionExcuteResult.isReject(decisionExcuteResult)) {
                return;
            }
            for (Map.Entry<Long, RuleExcuteResult> resultEntry
                    : decisionExcuteResult.getData().getHited().entrySet()) {
                String reason = null;
                if (resultEntry == null || resultEntry.getValue() == null) {
                    continue;
                }
                if (!CollectionUtils.isEmpty(resultEntry.getValue().getPromotMsgs())) {
                    while (resultEntry.getValue().getPromotMsgs().remove(null)) {
                    }
                }
                if (!CollectionUtils.isEmpty(resultEntry.getValue().getPromotMsgs())) {
                    reason = Joiner.on("\n").join(resultEntry.getValue().getPromotMsgs());
                    reason += "\n";
                }
                Long ruleId = Long.valueOf(resultEntry.getValue().getId());
                CheckRuleConfigEntity entity = ruleConfigDomainService.selectCheckRuleConfigById(ruleId);
                BcpCheckRuleAlertEntity alertEntity = getRuleAlertChannelConfig(ruleId);
                if (null != entity) {
                    String formatString = null;
                    if (null != alertEntity) {
                        alertEntity.setAppCode(entity.getAppCode());
                        formatString = alertEntity.getAlertTextFormat();
                    }
                    AlertMessageValueObject alertMessageValueObject = AlertMessageValueObject.transAlertMsg(
                            formatString, eventMessage.getRawBody(), entity.getRuleName());
                    if (StringUtils.isEmpty(reason)) {
                        alertMessageValueObject.setMessage(alertMessageValueObject.getMessage());
                    } else {
//                        alertMessageValueObject.setMessage(reason + alertMessageValueObject.getMessage());
                        alertMessageValueObject.setMessage(reason);
                    }
                    alertMessageDomainService.send(alertMessageValueObject, alertEntity);
                }
            }
        } catch (Exception e) {
            logger.info("发送消息失败", e);
        }
    }

    private void asyncSaveRecord(final EventMessageEntity<?> eventMessage, final DecisionExcuteResult decisionExcuteResult) {
        try {
            if (null == decisionExcuteResult) {
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int startTime = (int) Math.floor(System.currentTimeMillis() / 1000 / 60);

            if (DecisionExcuteResult.isException(decisionExcuteResult)) {
                for (Map.Entry<Long, RuleExcuteResult> resultEntry
                        : decisionExcuteResult.getData().getExceptioned().entrySet()) {
                    checkRuleStatService.incrExceptionCount(Math.toIntExact(resultEntry.getKey()), startTime);
                }
            } else if (DecisionExcuteResult.isPass(decisionExcuteResult)) {
                for (Map.Entry<Long, RuleExcuteResult> resultEntry
                        : decisionExcuteResult.getData().getHited().entrySet()) {
                    // todo
                }
            } else {
                for (Map.Entry<Long, RuleExcuteResult> resultEntry
                        : decisionExcuteResult.getData().getHited().entrySet()) {
                    checkRuleStatService.incrFailCount(Math.toIntExact(resultEntry.getKey()), startTime);

                    CheckFailRecordEntity checkFailRecordEntity = new CheckFailRecordEntity();
                    checkFailRecordEntity.setRuleId(resultEntry.getKey());
                    String reason = null;
                    if (!CollectionUtils.isEmpty(resultEntry.getValue().getPromotMsgs())) {
                        reason = Joiner.on("\n").join(resultEntry.getValue().getPromotMsgs());
                    }
                    checkFailRecordEntity.setEventMessage(eventMessage.getRawBody());
                    checkFailRecordEntity.setMessageId(eventMessage.getMessageId());

                    checkFailRecordEntity.setReason(reason == null ? "" : reason);
                    checkFailRecordService.add(checkFailRecordEntity);
                }
            }
        } catch (Exception e) {
            logger.error("保存执行记录异常", e);
        }
    }


    /**
     * 处理事件决策.
     */
    public DecisionExcuteResult decision(Long eventId, @NonNull EventMessageEntity<?> object) {
        logger.info("执行方法DecisionService.decision");
        DecisionExcuteResult finalResult = null;
        try {
            // 参数检查
            EventSourceConfigEntity entity = decisionParamCheck(eventId);
            // 决策前流程
            decisonPreHandle(entity, object);
            // 最终决策
            finalResult = decisionInner(entity, object);
        } catch (FailfastException failfastException) {
            //记录打印异常
            recordAndPrint(eventId, object, failfastException);
        } catch (Exception e) {
            logger.error("执行决策异常", e);
        } finally {
            logger.info("result {}", JSON.toJSONString(finalResult));
        }
        return finalResult;
    }

    private void recordAndPrint(Long eventId, EventMessageEntity<?> object, FailfastException failfastException) {
        logger.error("event is fail:{},{},{}", eventId, object.getMessageId(), failfastException.getMessage());
        if (StringUtils.isEmpty(failfastException.getPromotMsg())) {
            return;
        }
        MultiTagMonitor.record(MultiTagItem.build("busi-decision-event_" + eventId, "决策过程" + eventId)
            .addTag("reason", failfastException.getMessage()));
    }





    private EventSourceConfigEntity decisionParamCheck(Long eventId) {
        if (null == eventId || 0 == eventId) {
            return null;
        }
        // 查找规则信息配置, 需要加个索引
        EventSourceConfigEntity eventSourceConfigEntity = eventSourceConfigDomainService.getByIdWithCache(eventId);
        if (null == eventSourceConfigEntity
                || 0 == eventSourceConfigEntity.getId()
                || 0 != eventSourceConfigEntity.getIsDelete()) {
            throw new FailfastException(null, "事件无效");
        }
        return eventSourceConfigEntity;
    }

    private void decisonPreHandle(EventSourceConfigEntity entity, EventMessageEntity<?> message) {
        boolean isHandled = false;
        try {
            isHandled = executionChain.applyPreHandle(entity, message);
            if (isHandled) {
                throw new FailfastException(null, null);
            }
        } catch (FailfastException failfastException) {
            throw failfastException;
        } catch (Exception e) {
            logger.error("执行过滤器异常", e);
        }
    }


    /**
     * 处理事件决策.
     *
     * @param eventId            ""
     * @param eventMessageEntity ""
     * @return ""
     */
    public DecisionExcuteResult decisionImmediately(Long eventId, EventMessageEntity<?> eventMessageEntity) {
        DecisionExcuteResult finalResult = null;
        try {
            // 参数检查
            EventSourceConfigEntity entity = decisionParamCheck(eventId);
            // 决策前流程
            decisonImmediatelyPreHandle(entity, eventMessageEntity);
            // 最终决策
            finalResult = decisionInner(entity, eventMessageEntity);
        } catch (FailfastException failfastException) {
            if (!StringUtils.isEmpty(failfastException.getPromotMsg())) {
                MultiTagMonitor.record(MultiTagItem.build("busi-decision-event_" + eventId, "决策过程" + eventId)
                        .addTag("reason", failfastException.getMessage()));
            }
            logger.info("event is fail:{},{}", eventId, eventMessageEntity.getMessageId());
        } catch (Exception e) {
            logger.info("执行决策异常", e);
        } finally {
            logger.info("result {}", JSON.toJSONString(finalResult));
        }
        return finalResult;
    }

    private void decisonImmediatelyPreHandle(EventSourceConfigEntity entity, EventMessageEntity<?> message) {
        boolean isHandled = false;
        try {
            isHandled = executionAsyncChain.applyPreHandle(entity, message);
            if (isHandled) {
                throw new FailfastException(null, null);
            }
        } catch (Exception e) {
            logger.error("执行过滤器异常", e);
        }
    }


    /**
     * 处理事件决策.
     *
     * @param configEntity ""
     * @param object       ""
     * @return ""
     */
    @SuppressWarnings({"MethodLength"})
    private DecisionExcuteResult decisionInner(
            final EventSourceConfigEntity configEntity, final EventMessageEntity<?> object) {

        String eventId = String.valueOf(configEntity.getId());
        String name = "risk.bcp.event." + configEntity.getName();
        Supplier<DecisionExcuteResult> main = new Supplier<DecisionExcuteResult>() {
            @Override
            public DecisionExcuteResult get() {
                // 调用决策信息
                Map<String, String> context = Maps.newHashMap();
                FieldSetterReader.Setter.setFeild(context, Constant.EVENET_CODE, configEntity.getName());
                FieldSetterReader.Setter.setFeild(context, Constant.EVENET_ID, eventId);

                ContextWrap contextWrap = new ContextWrap(context);
                FieldSetterReader.Setter.setRuntimeField(
                        contextWrap.getRuntimeContext(), Constant.RT_RAW_SCRIPT_PARAM, object.getMessageBody());

                logger.info("调用决策信息:{}", JSON.toJSONString(contextWrap));
                DecisionExcuteResult decisionExcuteResult = decisionContext.invoke(contextWrap);
                logger.info("调用决策信息,结果:{}", JSON.toJSONString(decisionExcuteResult));

                return decisionExcuteResult;
            }
        };
        Supplier<DecisionExcuteResult> failover = new Supplier<DecisionExcuteResult>() {
            @Override
            public DecisionExcuteResult get() {
                BusinessMonitor.recordOne("business-sentinel-failover_" + name);
                return null;
            }
        };
        DecisionExcuteResult result = SentinelUtil.wrapSimpleInterface(name, main, failover);

        AsyncUtil.run(() -> {
            asyncSaveRecord(object, result);
        });

        if (DecisionExcuteResult.isReject(result)) {
            AsyncUtil.run(() -> {
                sendAlertMessage(object, result);
            });
        }
        if (null == result) {
            MultiTagMonitor.record(MultiTagItem.build(
                    EventSourceConfigEntityUtils.decisionMonitorId(eventId),
                    EventSourceConfigEntityUtils.decisionMonitorName(eventId)).addTag("result", "nullresult"));
        } else {
            if (DecisionExcuteResult.isReject(result)) {
                MultiTagMonitor.record(MultiTagItem.build(
                        EventSourceConfigEntityUtils.decisionMonitorId(eventId),
                        EventSourceConfigEntityUtils.decisionMonitorName(eventId)).addTag("result", "hited"));
            } else if (DecisionExcuteResult.isException(result)) {
                MultiTagMonitor.record(MultiTagItem.build(
                        EventSourceConfigEntityUtils.decisionMonitorId(eventId),
                        EventSourceConfigEntityUtils.decisionMonitorName(eventId)).addTag("result", "exeption"));
            } else {
                MultiTagMonitor.record(MultiTagItem.build(
                        EventSourceConfigEntityUtils.decisionMonitorId(eventId),
                        EventSourceConfigEntityUtils.decisionMonitorName(eventId)).addTag("result", "normal"));
            }
        }
        return result;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        initDecisonFlows();
    }

    private void initDecisonFlows() {
        executionChain.addInterceptor(statusInterceptor);
        executionChain.addInterceptor(sampleHandleInterceptor);
        executionChain.addInterceptor(messageDelayHandleInterceptor);
        executionChain.addInterceptor(messagePoolHandleInterceptor);

        executionAsyncChain.addInterceptor(statusInterceptor);
        executionAsyncChain.addInterceptor(messagePoolHandleInterceptor);
    }
}
