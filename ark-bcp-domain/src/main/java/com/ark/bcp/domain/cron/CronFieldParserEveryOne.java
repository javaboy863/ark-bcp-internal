

package com.ark.bcp.domain.cron;

import com.ark.bcp.domain.entity.CronFieldEntity;
import com.ark.bcp.domain.vo.cron.CronStrategyVO;
import org.apache.commons.lang3.StringUtils;

/**
 */
public class CronFieldParserEveryOne implements CronFieldParser {
    private static final String EVERY_ONE_CRON = "*";

    @Override
    public String makeCronExpress(CronFieldEntity cronFieldEntity) {
        if (null != cronFieldEntity && CronStrategyVO.EVERY_ONE == cronFieldEntity.getStrategy()) {
            return EVERY_ONE_CRON;
        }
        return null;
    }

    @Override
    public CronFieldEntity parseCronExpress(final String cron) {
        if (!StringUtils.isEmpty(cron) && EVERY_ONE_CRON.equalsIgnoreCase(cron.trim())) {
            return CronFieldEntity.builder().strategy(CronStrategyVO.EVERY_ONE).build();
        }
        return null;
    }
}
