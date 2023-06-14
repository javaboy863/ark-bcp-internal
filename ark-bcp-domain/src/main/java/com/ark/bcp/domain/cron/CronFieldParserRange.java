

package com.ark.bcp.domain.cron;

import com.ark.bcp.domain.entity.CronFieldEntity;
import com.ark.bcp.domain.vo.cron.CronStrategyVO;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 */
public class CronFieldParserRange implements CronFieldParser {
    private static final String RANGE_CRON = "-";

    @Override
    public String makeCronExpress(final CronFieldEntity cronFieldEntity) {
        if (null == cronFieldEntity) {
            return null;
        }
        if (CronStrategyVO.RANGE != cronFieldEntity.getStrategy()) {
            return null;
        }
        if (null == cronFieldEntity.getRangeFrom()) {
            return null;
        }
        if (null == cronFieldEntity.getRangeTo()) {
            return null;
        }
        return String.format("%s" + RANGE_CRON + "%s", cronFieldEntity.getRangeFrom(), cronFieldEntity.getRangeTo());
    }

    @SuppressWarnings("AlibabaUndefineMagicConstant")
    @Override
    public CronFieldEntity parseCronExpress(final String cron) {
        if (StringUtils.isEmpty(cron)) {
            return null;
        }
        List<String> ranges = Splitter.on(RANGE_CRON).splitToList(cron);
        if (ranges.size() != 2) {
            return null;
        }
        if (!StringUtils.isNumeric(ranges.get(0)) || !StringUtils.isNumeric(ranges.get(1))) {
            return null;
        }
        return CronFieldEntity.builder()
                .strategy(CronStrategyVO.RANGE)
                .rangeFrom(Integer.valueOf(ranges.get(0)))
                .rangeTo(Integer.valueOf(ranges.get(1)))
                .build();
    }
}
