

package com.ark.bcp.domain.cron;

import com.ark.bcp.domain.entity.CronFieldEntity;

/**
 */
public interface CronFieldParser {
    /**
     * cron 解析器
     *
     * @param cronFieldEntity
     * @return
     */
    String makeCronExpress(final CronFieldEntity cronFieldEntity);

    /**
     * cron 解析器
     *
     * @param cron
     * @return
     */
    CronFieldEntity parseCronExpress(final String cron);
}
