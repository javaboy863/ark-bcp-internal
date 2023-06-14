
package com.ark.bcp.app.utils;

import com.google.common.collect.Lists;
import com.missfresh.risk.bcp.domain.entity.CronFieldEntity;
import com.missfresh.risk.bcp.domain.vo.cron.CronStrategyVO;
import com.missfresh.risk.bcp.dto.CronFieldDto;
import com.missfresh.risk.bcp.dto.InspectionEventSourceConfigDto;

import java.util.List;

/**
 */
public class CronFieldEntitys {
    public static CronFieldDto transToDto(final CronFieldEntity sField) {
        return CronFieldDto.builder()
                .strategy(sField.getStrategy().getStrategy())
                .rangeFrom(sField.getRangeFrom())
                .rangeTo(sField.getRangeTo())
                .repeatFrom(sField.getRepeatFrom())
                .repeatTick(sField.getRepeatTick())
                .enums(sField.getEnums())
                .build();
    }

    public static CronFieldEntity transFromDto(final CronFieldDto cronFieldDto) {
        if (null == cronFieldDto) {
            return null;
        }
        CronStrategyVO cronStrategyVO = CronStrategyVO.transFromStrategy(cronFieldDto.getStrategy());
        return CronFieldEntity.builder()
                .strategy(cronStrategyVO)
                .rangeFrom(cronFieldDto.getRangeFrom())
                .rangeTo(cronFieldDto.getRangeTo())
                .repeatFrom(cronFieldDto.getRepeatFrom())
                .repeatTick(cronFieldDto.getRepeatTick())
                .enums(cronFieldDto.getEnums())
                .build();
    }

    public static List<CronFieldEntity> fromInspectionEventDto(final InspectionEventSourceConfigDto iescDto) {
        final List<CronFieldEntity> entities = Lists.newArrayList();
        // 秒，分，时，日，天 顺序不可调整
        entities.add(CronFieldEntitys.transFromDto(iescDto.getCronSecendField()));
        entities.add(CronFieldEntitys.transFromDto(iescDto.getCronMinuteField()));
        entities.add(CronFieldEntitys.transFromDto(iescDto.getCronHourField()));
        entities.add(CronFieldEntitys.transFromDto(iescDto.getCronDayField()));
        entities.add(CronFieldEntitys.transFromDto(iescDto.getCronMonthField()));
        return entities;
    }
}
