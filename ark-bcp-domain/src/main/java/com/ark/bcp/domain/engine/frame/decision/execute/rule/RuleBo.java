

package com.ark.bcp.domain.engine.frame.decision.execute.rule;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.engine.frame.decision.execute.condition.AbstractConditionBO;
import com.ark.bcp.domain.engine.frame.decision.execute.condition.ConditionFactory;
import com.ark.bcp.domain.engine.frame.decision.execute.condition.Conditions;
import com.ark.bcp.domain.engine.frame.config.impl.BcpConfigSetService;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.ExecuteResult;
import com.ark.bcp.domain.engine.frame.decision.execute.RuleExcuteResult;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.exception.IllegalParamException;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
public class RuleBo implements IRuleComponent {

    private Logger logger = LoggerFactory.getLogger(RuleBo.class);

    private AbstractConditionBO topLevelCondition = null;

    private volatile CheckRuleConfigEntity ruleEntity = null;

    private RuleExcuteResult newResultObject() {
        RuleExcuteResult ruleExcuteResult = new RuleExcuteResult();
        ruleExcuteResult.setMsg("");
        ruleExcuteResult.setId(ruleEntity.getId());
        ruleExcuteResult.setAvailable(true);
        ruleExcuteResult.setReuslt(false);
        return ruleExcuteResult;
    }

    @Override
    public RuleExcuteResult ruleExecute(ContextWrap contexts) {
        RuleExcuteResult ruleRes = newResultObject();

        try {
            // 实体合法性检查
            ruleExecuteParamCheck();
            // 判断开关
            ruleExecuteSkip();

            // 生成规则表达式，并调用express
            ExecuteResult filterResult = topLevelCondition.conditionInvoke(contexts);
            ruleRes.setAvailable(filterResult.getAvailable());
            ruleRes.setReuslt(filterResult.getReuslt());
            ruleRes.setPromotMsgs(filterResult.getPromotMsgs());

            if (logger.isDebugEnabled()) {
                logger.debug("rulebo exeresult:rule{}-{}, result{}", ruleEntity.getId(), ruleEntity.getRuleName(),
                        JSON.toJSONString(filterResult));
            }
            return ruleRes;
        } catch (FailfastException | IllegalParamException e) {
            ruleRes.setMsg(e.getMessage());
        } catch (Exception e) {
            logger.error("执行规则异常:{}", ruleRes.getId(), e);
        }
        return ruleRes;
    }

    private void ruleExecuteParamCheck() {
        if (null == ruleEntity) {
            throw new IllegalParamException("规则为空");
        }
        if (null == topLevelCondition) {
            throw new FailfastException(null, "没有配置条件");
        }
    }

    private void ruleExecuteSkip() {
        if (!ruleEntity.isEnable()) {
            throw new FailfastException(null, "规则未开启" + ruleEntity.getId());
        }
    }

    public void init(CheckRuleConfigEntity entity) {
        if (null == entity) {
            return;
        }
        ruleEntity = entity;
        if (logger.isDebugEnabled()) {
            logger.info("rule componet init:{}", JSON.toJSONString(entity));
        }
        logger.info("rule componet init:{}", entity.getId());
        // 获取所有条件信息
        List<ConditionConfigEntity> conditions = getAllConditonByRuleId(entity.getId());
        List<ConditionConfigEntity> top0Conditions = Conditions.filterTopConditionSet(conditions);
        ConditionConfigEntity topLevelCodiConfig = null;
        if (1 == top0Conditions.size()) {
            topLevelCodiConfig = top0Conditions.get(0);
        } else {
            logger.info("rule have more than 1 condition init:{}", entity);
        }
        if (null != topLevelCodiConfig) {
            topLevelCondition = ConditionFactory.createConditionBo(topLevelCodiConfig, conditions);
        }
    }

    private List<ConditionConfigEntity> getAllConditonByRuleId(Long ruleId) {
        BcpConfigSetService bcpConfigSetService = AbstractApplicationContextUtil.getExtension(BcpConfigSetService.class, BcpConfigSetService.SINGLETON_BEAN_NAME);
        if (null == bcpConfigSetService) {
            logger.warn("获取服务失败:BcpConfigSetService");
            return null;
        }
        if (null == ruleId) {
            logger.warn("规则id不能为空");
        }

        return bcpConfigSetService.getConditionsByRuleId(ruleId);
    }
}
