

package com.ark.bcp.app.utils;

import com.google.common.collect.Lists;
import com.missfresh.risk.bcp.domain.entity.InspectionEventSourceEntity;
import com.missfresh.risk.bcp.domain.vo.LoadDataLogInfoVO;
import com.missfresh.risk.bcp.dto.InspectionEventSourceConfigDto;

/**
 */
public class InspectionEventSourceEntitys {

    public static InspectionEventSourceConfigDto toInspectionEventSourctDto(final InspectionEventSourceEntity entity) {
        return InspectionEventSourceConfigDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .cron(entity.getCron())
                .status(entity.getStatus())
                .sampleRatio(entity.getSampleRatio())
                .loadDataStrategy(entity.getLoadDataStrategy())
                .loadDataScript(LoadDataGroovyScripts.toLoadDataGroovyScriptDto(entity.getLoadDataGroovyScriptVO()))
                .loadDataTemplates(LoadDataTemplates.toLoadDataTemplateDtos(entity.getLoadDataTemplateVOList()))
                .logInfo(LoadDataLogInfoVOs.toLogInfoDto(entity.getLoadDataLogInfoVo()))
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .version(entity.getVersion())
                .build();
    }

    public static InspectionEventSourceEntity fromInspectionEventSourctDto(final InspectionEventSourceConfigDto configDto) {
        InspectionEventSourceEntity entity = new InspectionEventSourceEntity();
        entity.setCron(configDto.getCron());
        entity.setLoadDataStrategy(configDto.getLoadDataStrategy());
        entity.setLoadDataGroovyScriptVO(LoadDataGroovyScripts.fromLoadDataGroovyScriptDto(configDto.getLoadDataScript()));
        entity.setLoadDataTemplateVOList(LoadDataTemplates.fromLoadDataTemplateDtos(configDto.getLoadDataTemplates()));
        entity.setLoadDataLogInfoVo(LoadDataLogInfoVOs.fromLoadDataLoginfoDtos(configDto.getLogInfo()));
        entity.setId(configDto.getId());
        entity.setName(configDto.getName());
        entity.setDescription(configDto.getName());
        entity.setSampleRatio(configDto.getSampleRatio());
        entity.setCreateTime(configDto.getCreateTime());
        entity.setUpdateTime(configDto.getUpdateTime());
        entity.setCreateUser(configDto.getCreateUser());
        entity.setUpdateUser(configDto.getUpdateUser());
        entity.setVersion(configDto.getVersion());
        entity.setTriggerType(1);

        return entity;
    }
}
