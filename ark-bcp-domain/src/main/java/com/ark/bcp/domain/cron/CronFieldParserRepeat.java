

package com.ark.bcp.domain.cron;

import com.ark.bcp.domain.entity.CronFieldEntity;
import com.ark.bcp.domain.vo.cron.CronStrategyVO;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 */
public class CronFieldParserRepeat implements CronFieldParser{
    private static final String REPEAT_CRON = "/";

    @Override
    public String makeCronExpress(final CronFieldEntity cronFieldEntity) {
        if (null == cronFieldEntity) {
            return null;
        }
        if (CronStrategyVO.REPEAT != cronFieldEntity.getStrategy()) {
            return null;
        }
        if (null == cronFieldEntity.getRepeatFrom()) {
            return null;
        }
        if (null == cronFieldEntity.getRepeatTick()) {
            return null;
        }
        return String.format("%s" + REPEAT_CRON + "%s", cronFieldEntity.getRepeatFrom(), cronFieldEntity.getRepeatTick());
    }

    @SuppressWarnings("AlibabaUndefineMagicConstant")
    @Override
    public CronFieldEntity parseCronExpress(final String cron) {
        if (StringUtils.isEmpty(cron)) {
            return null;
        }
        List<String> ranges = Splitter.on(REPEAT_CRON).splitToList(cron);
        if (ranges.size() != 2) {
            return null;
        }
        if (!StringUtils.isNumeric(ranges.get(0)) || !StringUtils.isNumeric(ranges.get(1))) {
            return null;
        }
        return CronFieldEntity.builder()
                .strategy(CronStrategyVO.REPEAT)
                .repeatFrom(Integer.valueOf(ranges.get(0)))
                .repeatTick(Integer.valueOf(ranges.get(1)))
                .build();
    }
}
