package com.ark.bcp.domain.service;

import com.ark.bcp.domain.repository.riskbcp.ConditionConfigRepository;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 */
@Slf4j
@Service
public class ConditionConfigService {

    @Resource
    private ConditionConfigRepository conditionConfigRepository;

    public void addCondition(ConditionConfigEntity conditionConfigEntity) {
        log.debug("save condition:{}", conditionConfigEntity);
        conditionConfigRepository.insertConditionConfig(conditionConfigEntity);
    }

    public List<ConditionConfigEntity> findConditionConfigByRuleId(Long ruleId) {
        ConditionConfigEntity entity = ConditionConfigEntity.builder()
                .ruleId(ruleId)
                .build();
        List<ConditionConfigEntity> entityList = conditionConfigRepository.selectConditionConfigList(entity);
        log.info("ruleId:{}, result:{}", ruleId, entityList);
        return entityList;
    }

    public int update(ConditionConfigEntity conditionConfigEntity) {
        return conditionConfigRepository.updateConditionConfigByIdSelective(conditionConfigEntity);
    }

    public ConditionConfigEntity findConditionConfigByRuleIdAndType(Long ruleId, Integer type) {
        ConditionConfigEntity entity = ConditionConfigEntity.builder()
                .ruleId(ruleId)
                .type(type)
                .build();
        List<ConditionConfigEntity> entityList = conditionConfigRepository.selectConditionConfigList(entity);
        log.info("ruleId:{}, type:{}, result:{}", ruleId, type, entityList);
        return entityList.get(0);
    }
}
