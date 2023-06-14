

package com.ark.bcp.domain.cron;

import com.ark.bcp.domain.entity.CronFieldEntity;
import com.ark.bcp.domain.vo.cron.CronStrategyVO;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 */
public class CronFieldParserEnumable implements CronFieldParser {
    private static final String ENUMABLE_CRON = ",";

    @Override
    public String makeCronExpress(final CronFieldEntity cronFieldEntity) {
        if (null == cronFieldEntity) {
            return null;
        }
        if (CronStrategyVO.ENUMABLE != cronFieldEntity.getStrategy()) {
            return null;
        }
        if (CollectionUtils.isEmpty(cronFieldEntity.getEnums())) {
            return null;
        }
        if (cronFieldEntity.getEnums().stream().anyMatch(Objects::isNull)) {
            return null;
        }
        return Joiner.on(ENUMABLE_CRON).join(cronFieldEntity.getEnums());
    }

    @Override
    public CronFieldEntity parseCronExpress(String cron) {
        if (StringUtils.isEmpty(cron)) {
            return null;
        }
        List<String> enums = Splitter.on(ENUMABLE_CRON).omitEmptyStrings().splitToList(cron);
        if (CollectionUtils.isEmpty(enums)) {
            return null;
        }
        List<Integer> enumValues = Lists.newArrayList();
        for (String anEnum : enums) {
            if (!StringUtils.isNumeric(anEnum)) {
                // 所有字段均为数字，遇到非数字即解析失败
                return null;
            }
            enumValues.add(Integer.valueOf(anEnum));
        }
        return CronFieldEntity.builder()
                .strategy(CronStrategyVO.ENUMABLE)
                .enums(enumValues)
                .build();
    }
}
