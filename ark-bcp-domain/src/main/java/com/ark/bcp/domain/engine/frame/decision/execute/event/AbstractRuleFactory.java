

package com.ark.bcp.domain.engine.frame.decision.execute.event;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.engine.frame.ComponentTypeEnum;
import com.ark.bcp.domain.engine.frame.config.impl.BcpConfigSetService;
import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.decision.execute.rule.IRuleComponent;
import com.ark.bcp.domain.engine.frame.decision.execute.rule.RuleBo;
import com.ark.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.ark.bcp.domain.engine.frame.rss.FailbackTask;
import com.ark.bcp.domain.engine.frame.rss.NotifyListener;
import com.ark.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import com.ark.bcp.domain.util.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 */
public abstract class AbstractRuleFactory implements NotifyListener, FailbackTask {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRuleFactory.class);
    private final Object ruleMapSyncObject = new Object();

    /**
     * 规则管理工具
     */
    private volatile Map<Long, IRuleComponent> asyncInvokeableSet = Maps.newConcurrentMap();


    public Map<Long, IRuleComponent> getAsyncInvokeableSet() {
        return asyncInvokeableSet;
    }

    /**
     * 重新加载规则集
     *
     * @return
     */
    protected abstract List<CheckRuleConfigEntity> refresh();

    @Override
    public void onFailback() {
        // 对比配置，
        Stopwatch stopwatch = Stopwatch.createStarted();
        synchronized (ruleMapSyncObject) {
            List<CheckRuleConfigEntity> ruleConfigEntities = refresh();
            refreshRuleMaps(ruleConfigEntities);
        }
        logger.info("重建规则集合,快照:{},total:{}", asyncInvokeableSet.keySet(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    @Override
    public void release() {
    }

    @Override
    public void onNotify(String topic, String serializable) {
        if (!Constant.BROADCAST_ID_EVENT.equalsIgnoreCase(topic)) {
            return;
        }
        try {
            PublishParams params = JSON.parseObject(serializable, PublishParams.class);
            if (null == params || ComponentTypeEnum.RULE != params.getComponentTypeEnum()) {
                return;
            }
            Long eventId = Long.valueOf(params.getTypeId());
            asyncInvokeableSet.remove(eventId);
            logger.info("on command:{}", JSON.toJSONString(params));
            if (params.getAction() != PublishParams.ActionType.DEL) {
                upsertRuleBo(eventId);
            }
        } catch (Exception e) {
            // donothing
            logger.info("配置变更转换失败:", e);
        }
    }

    public void releaseNotify() {
        logger.info("解除配置变更订阅:{}", this);
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        if (null == configChangeNotifier) {
            logger.warn("获取事件订阅服务异常");
            return;
        }
        // 订阅事件
        configChangeNotifier.unsubscribe(Constant.BROADCAST_ID_EVENT, this);
        // 订阅回调
        configChangeNotifier.unRegistFallbackTask(Constant.BROADCAST_ID_EVENT, this);
    }

    protected void initNotify() {
        logger.info("配置变更订阅:{}", this);
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        if (null == configChangeNotifier) {
            logger.warn("获取事件订阅服务异常");
            return;
        }
        // 订阅事件
        configChangeNotifier.subscribe(Constant.BROADCAST_ID_EVENT, this);
        // 订阅回调
        configChangeNotifier.doRegistFallbackTask(Constant.BROADCAST_ID_EVENT, this);
    }

    protected void refreshRuleMapsInner(List<CheckRuleConfigEntity> checkRuleConfigEntities) {
        if (null != checkRuleConfigEntities) {
            Map<Long, IRuleComponent> newRuleSet = Maps.newConcurrentMap();
            for (CheckRuleConfigEntity entity : checkRuleConfigEntities) {
                RuleBo ruleBo = new RuleBo();
                ruleBo.init(entity);
                newRuleSet.put(entity.getId(), ruleBo);
            }
            Map oldRuleSet = asyncInvokeableSet;
            asyncInvokeableSet = newRuleSet;
            if (null != oldRuleSet) {
                oldRuleSet.clear();
            }
        }
    }

    protected void refreshRuleMaps(List<CheckRuleConfigEntity> checkRuleConfigEntities) {
        synchronized (ruleMapSyncObject) {
            refreshRuleMapsInner(checkRuleConfigEntities);
        }
    }

    private IRuleComponent upsertRuleBo(Long ruleid) {
        synchronized (ruleMapSyncObject) {
            logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "upsertRuleBo lock:{}", ruleid);
            IRuleComponent ruleComponent = asyncInvokeableSet.get(ruleid);
            if (null != ruleComponent) {
                logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "upsertRuleBo lock retry:{}", ruleid);
                return ruleComponent;
            }
            try {
                BcpConfigSetService bcpConfigSetService = AbstractApplicationContextUtil.getExtension(BcpConfigSetService.class, null);
                if (null == bcpConfigSetService) {
                    logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "upsertRuleBo bcpconfigset 未找到");
                    return null;
                }
                CheckRuleConfigEntity entity = bcpConfigSetService.getRuleById(String.valueOf(ruleid));
                if (null != entity) {
                    RuleBo ruleBo = new RuleBo();
                    ruleBo.init(entity);
                    asyncInvokeableSet.put(ruleid, ruleBo);
                    logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "upsertRuleBo new ruleBo:{}", ruleid);
                } else {
                    logger.warn(Namespace.ENGINE_CONFIG_LOG_PREFIX + "upsertRuleBo null entity:{}", ruleid);
                }
            } catch (Exception e) {
                logger.error(Namespace.ENGINE_CONFIG_LOG_PREFIX + "upsertRuleBo error", e);
            }
        }
        return asyncInvokeableSet.get(ruleid);
    }
}
