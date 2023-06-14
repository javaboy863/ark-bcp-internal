package com.ark.bcp.domain.service;

import com.ark.bcp.domain.entity.DynamicCodeConfigEntity;
import com.ark.bcp.domain.repository.riskbcp.ConditionConfigRepository;
import com.ark.bcp.domain.repository.riskbcp.DynamicCodeConfigRepository;
import com.ark.bcp.domain.vo.BaseDynamicScriptVO;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
@Service
@Slf4j
public class DynamicCodeConfigService {

    @Resource
    private DynamicCodeConfigRepository dynamicCodeConfigRepository;

    @Resource
    private ConditionConfigRepository conditionConfigRepository;

    public int addCode(DynamicCodeConfigEntity codeConfigEntity) {
        log.debug("add code:{}", codeConfigEntity);
        codeConfigEntity.zipCompressSrc();
        return dynamicCodeConfigRepository.insertDynamicCodeConfig(codeConfigEntity);
    }

    public DynamicCodeConfigEntity queryCodeListById(Long codeId) {
        log.debug("queryCodeList, codeid:{}", codeId);
        DynamicCodeConfigEntity entity = dynamicCodeConfigRepository.selectBcpDynamicCodeConfigById(codeId);
        log.debug("query codeList by Id:{}, result:{}", codeId, entity);
        entity.unzipCompressSrc();
        return entity;
    }

    public List<DynamicCodeConfigEntity> queryCodeListByRuleId(Long ruleid) {
        log.debug("queryCodeList, conditionId:{}", ruleid);

        List<ConditionConfigEntity> entities = conditionConfigRepository.selectConditionConfigList(
                ConditionConfigEntity.builder().ruleId(ruleid).build());
        List<DynamicCodeConfigEntity> codes = entities.stream().map(conditionConfigEntity -> {
            BaseDynamicScriptVO parameter = BaseDynamicScriptVO.newInstance(conditionConfigEntity.getParams());
            if (null == parameter || StringUtils.isEmpty(parameter.getScriptId())) {
                return null;
            }
            DynamicCodeConfigEntity dynamicCodeConfigEntity = DynamicCodeConfigEntity.builder()
                    .id(Long.valueOf(parameter.getScriptId()))
                    .isDelete(0)
                    .build();
            List<DynamicCodeConfigEntity> tmpCodes = dynamicCodeConfigRepository.selectDynamicCodeConfigList(
                    dynamicCodeConfigEntity);
            DynamicCodeConfigEntity code = CollectionUtils.isEmpty(tmpCodes) ? null : tmpCodes.get(0);
            if (null != code) {
                code.unzipCompressSrc();
            }
            return code;
        }).collect(Collectors.toList());
        codes.removeAll(Collections.singleton(null));
        log.debug("query codeList by conditionId:{}, entity:{}, result:{}", ruleid, codes, entities);

        return codes;
    }

    public DynamicCodeConfigEntity queryCodeListByEventId(Long eventId) {
        log.debug("queryCodeListByEventId, eventid:{}", eventId);

        DynamicCodeConfigEntity dynamicCodeConfigEntity = DynamicCodeConfigEntity.builder()
                .eventId(eventId)
                .isDelete(0)
                .build();
        List<DynamicCodeConfigEntity> codeConfigList = dynamicCodeConfigRepository.selectDynamicCodeConfigList(
                dynamicCodeConfigEntity);
        while (codeConfigList.remove(null)) {

        }
        log.debug("query codeList by eventid:{}, result:{}", eventId, codeConfigList);
        return CollectionUtils.isEmpty(codeConfigList) ? null : codeConfigList.get(0);
    }

    public int update(DynamicCodeConfigEntity entity) {
        entity.zipCompressSrc();
        return dynamicCodeConfigRepository.updateDynamicCodeConfigByIdSelective(entity);
    }
}
