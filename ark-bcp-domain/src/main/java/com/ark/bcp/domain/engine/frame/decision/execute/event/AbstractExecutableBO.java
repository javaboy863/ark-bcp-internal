

package com.ark.bcp.domain.engine.frame.decision.execute.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.DecisionExcuteResult;
import com.ark.bcp.domain.engine.frame.decision.execute.FinalDescEnum;
import com.ark.bcp.domain.engine.frame.decision.execute.IDecisionContext;
import com.ark.bcp.domain.engine.frame.decision.execute.RuleExcuteResult;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.*;
import com.ark.bcp.domain.engine.frame.decision.execute.rule.IRuleComponent;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 一个策略集合.
 * 每个集合一个实例
 * 负责管理规则，生成规则表达式，规则编排
 *
 */
public abstract class AbstractExecutableBO extends AbstractRuleFactory implements IDecisionContext {

    private final Logger logger = LoggerFactory.getLogger(AbstractExecutableBO.class);

    private static ThreadPoolExecutor rulesExecutorService = new ThreadPoolExecutor(
            200, 400, 600L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(5120),
            new ThreadFactoryBuilder().setNameFormat("EXECUTE-RULE-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy());


    protected void init(List<CheckRuleConfigEntity> rules) {
        try {
            if (!CollectionUtils.isEmpty(rules)) {
                refreshRuleMaps(rules);
            }
            initNotify();
            logger.info("AbstractExecutableBO init over");
        } catch (Exception e) {
            logger.error("init AbstractExecutableBO Error", e);
        }
    }


    @Override
    public DecisionExcuteResult invoke(ContextWrap contexts) {
        logger.info("invoke:{}", JSONObject.toJSONString(contexts));
        DecisionExcuteResult result = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            // 入参检查
            invokeParamCheck(contexts);
            result = asyncInvoke(contexts);
        } catch (FailfastException e) {
            logger.error("快速失败:{}", e.getMessage());
        } catch (Exception e) {
            logger.error("执行异常", e);
        } finally {
            logger.info("invoke result:{}, total:{}", JSON.toJSONString(result), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
        return result;
    }

    private void invokeParamCheck(final ContextWrap contextWrap) {
        if (null == contextWrap
                || CollectionUtils.isEmpty(contextWrap.getContext())) {
            throw new FailfastException(null, "上下文为空");
        }
        if (CollectionUtils.isEmpty(getAsyncInvokeableSet())) {
            throw new FailfastException(null, "不存在规则");
        }
    }

    private DecisionExcuteResult asyncInvoke(final ContextWrap contextWrap) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(getAsyncInvokeableSet().size());
            Stopwatch stopwatch = Stopwatch.createStarted();
            // ruleid <> rule result
            ConcurrentMap<Long, RuleExcuteResult> localThreadHitedRules = Maps.newConcurrentMap();
            ConcurrentMap<Long, RuleExcuteResult> localThreadExceptedRules = Maps.newConcurrentMap();
            for (Map.Entry<Long, IRuleComponent> entry : getAsyncInvokeableSet().entrySet()) {
                Runnable runnable = () -> {
                    try {
                        RuleExcuteResult result = entry.getValue().ruleExecute(contextWrap);
                        // 规则命中，丢入命中队列并计算值
                        if (RuleExcuteResult.isHited(result)) {
                            localThreadHitedRules.putIfAbsent(entry.getKey(), result);
                        }
                        // 规则如果异常了，丢进异常队列
                        if (RuleExcuteResult.isFail(result)) {
                            localThreadExceptedRules.putIfAbsent(entry.getKey(), result);
                        }
                    } catch (Exception e) {
                        logger.error("执行规则异常", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                };
                rulesExecutorService.execute(runnable);
            }
            boolean bfinished = countDownLatch.await(5000, TimeUnit.MILLISECONDS);
            if (!bfinished) {
                logger.info("规则执行超时");
            }
            DecisionExcuteResult.DecisionExcuteData excuteData = DecisionExcuteResult.DecisionExcuteData.builder()
                    .hited(localThreadHitedRules)
                    .exceptioned(localThreadExceptedRules)
                    .build();
            DecisionExcuteResult decisionExcuteResult = DecisionExcuteResult.builder().data(excuteData).expend(stopwatch.elapsed(TimeUnit.MILLISECONDS)).build();
            if(!CollectionUtils.isEmpty(localThreadHitedRules)){
                decisionExcuteResult.setDecision(FinalDescEnum.REJECT.getCode());
            }
            return decisionExcuteResult;
        } catch (Exception e) {
            logger.info("SmartInvokeLayout countDownLatch error", e);
        }
        return null;
    }
}
