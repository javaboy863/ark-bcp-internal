

package com.ark.bcp.domain.engine.frame.decision.execute.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.engine.frame.config.impl.BcpConfigSetService;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 一个策略集合.
 * 每个集合一个实例
 * 负责管理规则，生成规则表达式，规则编排
 *
 */
public class EventBo extends AbstractExecutableBO {

    private Logger logger = LoggerFactory.getLogger(EventBo.class);
    private EventSourceConfigEntity eventEntity = null;

    public EventBo(EventSourceConfigEntity eventEntity) {
        this.eventEntity = eventEntity;
        init(eventEntity);
    }

    private void init(EventSourceConfigEntity eventEntity) {
        try {
            BcpConfigSetService bcpConfigSetService = AbstractApplicationContextUtil.getExtension(BcpConfigSetService.class, BcpConfigSetService.SINGLETON_BEAN_NAME);
            if (null == bcpConfigSetService) {
                logger.warn("获取服务失败:BcpConfigSetService");
                return;
            }
            List<CheckRuleConfigEntity> rules = bcpConfigSetService.getRulesByEventId(eventEntity.getId());
            init(rules);
            logger.info("strategySetBo init over, entity:{}", JSONObject.toJSONString(eventEntity));
        } catch (Exception e) {
            logger.error("init StrategySet Error", e);
        }
    }

    @Override
    protected List<CheckRuleConfigEntity> refresh() {
        try {
            BcpConfigSetService bcpConfigSetService = AbstractApplicationContextUtil.getExtension(BcpConfigSetService.class, BcpConfigSetService.SINGLETON_BEAN_NAME);
            if (null == bcpConfigSetService) {
                logger.warn("获取服务失败:BcpConfigSetService");
                return null;
            }
            List<CheckRuleConfigEntity> entities = bcpConfigSetService.getRulesByEventId(eventEntity.getId());
            if (logger.isDebugEnabled()) {
                logger.debug("refresh event rules:{}", JSON.toJSONString(entities));
            }
            return entities;
        } catch (Exception e) {
            logger.error("refresh EventBo Error", e);
        }
        logger.warn("refresh EventBo null");
        return null;
    }
}
