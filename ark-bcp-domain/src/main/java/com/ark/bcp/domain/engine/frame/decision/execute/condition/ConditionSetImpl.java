
package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.ExecuteResult;
import com.ark.bcp.domain.engine.frame.decision.execute.LogicOperatorEnum;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.exception.IllegalParamException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 条件组Bo.
 *
 */
public class ConditionSetImpl extends AbstractConditionBO {
    private static Logger logger = LoggerFactory.getLogger(ConditionSetImpl.class);

    private ConcurrentMap<Long, AbstractConditionBO> conditionMaps = Maps.newConcurrentMap();

    public ConditionSetImpl(final ConditionConfigEntity conditionConfigEntity,
                            final List<ConditionConfigEntity> conditions) {
        super(conditionConfigEntity);
        init(conditionConfigEntity, conditions);
    }

    private ExecuteResult fastFalse(final ContextWrap contextWrap, final LogicOperatorEnum logicOperatorEnum) {
        Boolean finalResult = true;
        List<String> promots = null;
        for (Map.Entry<Long, AbstractConditionBO> entry : conditionMaps.entrySet()) {
            ExecuteResult itemResult = entry.getValue().conditionInvoke(contextWrap);
            // 遇到异常终止执行，整体检查结果为异常
            if (null == itemResult || !itemResult.getAvailable()) {
                return ExecuteResult.exception(null == itemResult ? null : itemResult.getPromotMsgs());
            }
            if (logicOperatorEnum == LogicOperatorEnum.AND) {
                finalResult = finalResult & itemResult.getReuslt();
            } else {
                finalResult = finalResult & !itemResult.getReuslt();
            }
            if (!finalResult) {
                // 快速返回
                return ExecuteResult.notHited();
            } else if (!CollectionUtils.isEmpty(itemResult.getPromotMsgs())) {
                if (null == promots) {
                    promots = Lists.newArrayList();
                }
                promots.addAll(itemResult.getPromotMsgs());
            }
        }
        return ExecuteResult.hited(promots);
    }

    private ExecuteResult fastTrue(ContextWrap contextWrap, LogicOperatorEnum logicOperatorEnum) {
        Boolean finalResult = false;
        for (Map.Entry<Long, AbstractConditionBO> entry : conditionMaps.entrySet()) {
            ExecuteResult itemResult = entry.getValue().conditionInvoke(contextWrap);
            // 遇到异常终止执行，整体检查结果为异常
            if (null == itemResult || !itemResult.getAvailable()) {
                return ExecuteResult.exception(null == itemResult ? null : itemResult.getPromotMsgs());
            }
            if (logicOperatorEnum == LogicOperatorEnum.OR) {
                finalResult = finalResult | itemResult.getReuslt();
            } else {
                finalResult = finalResult | !itemResult.getReuslt();
            }
            if (finalResult) {
                // 快速返回
                return ExecuteResult.hited(itemResult.getPromotMsgs());
            }
        }
        return ExecuteResult.notHited();
    }

    @Override
    protected Object innerConditionInvoke(ContextWrap contextWrap) {
        try {
            ConditionGroupParameter paramObject = JSON.parseObject(getConditionConfigEntity().getParams(), ConditionGroupParameter.class);

            preExpressCheck(contextWrap, paramObject);

            // 没有条件，正常返回
            if (MapUtils.isEmpty(conditionMaps)) {
                return ExecuteResult.notHited();
            }

            LogicOperatorEnum operatorEnum = LogicOperatorEnum.getByCode(paramObject.getLogicOperator());
            // 如果有多个条件，按照条件顺序 one by one 执行。视条件组的逻辑操作符判定取哪个条件的返回结果.
            if (LogicOperatorEnum.AND == operatorEnum
                    || LogicOperatorEnum.ALL_NOT_MATCH == operatorEnum) {
                return fastFalse(contextWrap, operatorEnum);
            } else {
                return fastTrue(contextWrap, operatorEnum);
            }
        } catch (FailfastException | IllegalParamException e) {
            logger.info("快速失败:{}", e.getMessage());
            // 不满足执行条件，按照正常返回
            return ExecuteResult.notHited();
        } catch (Exception e) {
            logger.info("执行异常", e);
            // 不满足执行条件，按照正常返回
            return ExecuteResult.notHited();
        }
    }


    private void init(final ConditionConfigEntity conditionConfigEntity,
                      final List<ConditionConfigEntity> conditions) {
        try {
            // 获取本set下的所有子条件
            List<ConditionConfigEntity> subConditions = Conditions.filterConditions(conditions, conditionConfigEntity.getId());
            if (CollectionUtils.isEmpty(subConditions)) {
                return;
            }

            for (ConditionConfigEntity subCondition : subConditions) {
                AbstractConditionBO subConditonBo = ConditionFactory.createConditionBo(subCondition, conditions);
                if (null != subConditonBo) {
                    conditionMaps.putIfAbsent(subCondition.getId(), subConditonBo);
                } else {
                    logger.warn("非法条件配置:{}", JSON.toJSONString(subCondition));
                }
            }
        } catch (Exception e) {
            logger.error("初始化异常", e);
        }
    }

    private void preExpressCheck(ContextWrap contexts, ConditionGroupParameter conditionGroupParameter) {
        if (null == conditionGroupParameter || null == contexts) {
            throw new IllegalParamException("参数错误");
        }

        int logicOperator = conditionGroupParameter.getLogicOperator();
        LogicOperatorEnum logicOperatorEnum = LogicOperatorEnum.getByCode(logicOperator);
        if (null == logicOperatorEnum) {
            throw new FailfastException(null, "非法的逻辑操作符号");
        }
    }
}
