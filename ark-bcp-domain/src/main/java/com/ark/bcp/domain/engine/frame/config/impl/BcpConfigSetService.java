

package com.ark.bcp.domain.engine.frame.config.impl;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.engine.frame.decision.execute.condition.BaseDynamicScriptParameter;
import com.ark.bcp.domain.engine.frame.decision.execute.condition.ConditionTypeEnum;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import com.ark.bcp.domain.entity.DynamicCodeConfigEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.repository.riskbcp.CheckRuleConfigRepository;
import com.ark.bcp.domain.repository.riskbcp.ConditionConfigRepository;
import com.ark.bcp.domain.repository.riskbcp.DynamicCodeConfigRepository;
import com.ark.bcp.domain.repository.riskbcp.EventSourceConfigRepository;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.ark.bcp.domain.engine.frame.config.impl.BcpConfigSetService.SINGLETON_BEAN_NAME;

/**
 */
@Service(value = SINGLETON_BEAN_NAME)
public class BcpConfigSetService implements InitializingBean {

    public static final String SINGLETON_BEAN_NAME = "bcpConfigSetApi";

    private static final Logger logger = LoggerFactory.getLogger(BcpConfigSetService.class);

    @Resource
    private ConditionConfigRepository conditionConfigRepository;

    @Resource
    private EventSourceConfigRepository eventSourceConfigRepository;

    @Resource
    private CheckRuleConfigRepository checkRuleConfigRepository;

    @Resource
    private DynamicCodeConfigRepository dynamicCodeConfigRepository;

    public List<ConditionConfigEntity> getConditionsByRuleId(Long id) {

        ConditionConfigEntity params = new ConditionConfigEntity();
        params.setRuleId(id);
        params.setIsDelete(0);
        List<ConditionConfigEntity> configEntities = conditionConfigRepository.selectConditionConfigList(params);
        if (CollectionUtils.isEmpty(configEntities)) {
            return null;
        }

        fillParams(configEntities);

        return configEntities;
    }

    private void fillParams(List<ConditionConfigEntity> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        configs.forEach(conditionConfig -> {
            final ConditionTypeEnum typeEnum = ConditionTypeEnum.getByCode(conditionConfig.getType());
            if (null == typeEnum) {
                logger.info("unknow conditon:{}", JSON.toJSONString(conditionConfig));
                return;
            }
            switch (typeEnum) {
                case QLEXPRESS_CONDITION:
                case GROORY_SCRIPT_CONDITION: {
                    BaseDynamicScriptParameter parameter = JSON.parseObject(conditionConfig.getParams(), BaseDynamicScriptParameter.class);
                    DynamicCodeConfigEntity entity = dynamicCodeConfigRepository.selectBcpDynamicCodeConfigById(Long.valueOf(parameter.getScriptId()));
                    if (null != entity && !StringUtils.isEmpty(entity.getScriptContent())) {
                        entity.unzipCompressSrc();
                        parameter.setRawScriptSource(entity.getScriptContent());
                    }
                    conditionConfig.setParams(JSON.toJSONString(parameter));
                    break;
                }
                default: {

                }
            }
        });
    }

    public EventSourceConfigEntity getEventConfigById(Long id) {
        try {
            return eventSourceConfigRepository.selectById(id);
        } catch (Exception e) {
            logger.error("获取事件异常", e);
        }
        return null;
    }

    public List<CheckRuleConfigEntity> getRulesByEventId(Long id) {
        try {
            CheckRuleConfigEntity checkRuleConfigEntity = new CheckRuleConfigEntity();
            checkRuleConfigEntity.setEventId(id);
            checkRuleConfigEntity.setIsDelete(0);
            List<CheckRuleConfigEntity> entities =
                    checkRuleConfigRepository.selectCheckRuleConfigList(checkRuleConfigEntity);
            if (CollectionUtils.isEmpty(entities)) {
                return null;
            }
            logger.info("load rules by event:{},size:{}", id, entities.size());
            return entities;
        } catch (Exception e) {
            logger.error("获取规则异常", e);
        }
        return null;
    }

    public CheckRuleConfigEntity getRuleById(String id) {

        try {
            return checkRuleConfigRepository.selectCheckRuleConfigById(Long.parseLong(id));
        } catch (Exception e) {
            logger.error("获取规则异常", e);
        }

        return null;
    }


    public ConditionConfigEntity getConditionConfigById(String id) {
        try {
            ConditionConfigEntity entity = conditionConfigRepository.selectConditionConfigById(Long.valueOf(id));
            if (null != entity) {
                fillParams(Lists.newArrayList(entity));
                return entity;
            }
        } catch (Exception e) {
            logger.error("查找规则异常", e);
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        getConditionConfigById("91");
    }
}
