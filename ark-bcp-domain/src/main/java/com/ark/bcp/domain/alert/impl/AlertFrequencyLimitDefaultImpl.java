

package com.ark.bcp.domain.alert.impl;

import com.ark.bcp.domain.entity.BcpAlertConfigEntity;
import com.ark.bcp.domain.util.TimeUnitEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 */
@Service("default")
public class AlertFrequencyLimitDefaultImpl extends AbstractAlertFrequencyLimit {
    private static final Logger logger = LoggerFactory.getLogger(AlertFrequencyLimitDefaultImpl.class);

    private static final Integer DEFAULT_LIMIT_COUNT_PERMIN = 1;
    private static final Integer DEFAULT_LIMIT_SLIDWIN_MIN = 1;

    private static final String ALERT_REPEATLIMIT_PREFIX = "bcp:alert:rt:";

    private String getRepeatKey(BcpAlertConfigEntity alertConfigEntity) {
        if (null == alertConfigEntity || StringUtils.isEmpty(alertConfigEntity.getBindRuleId())) {
            return null;
        }
        return ALERT_REPEATLIMIT_PREFIX + alertConfigEntity.getBindRuleId();
    }

    @Override
    public boolean isLimited(BcpAlertConfigEntity alertConfigEntity) {
        String key = getRepeatKey(alertConfigEntity);
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        long repeatValue = getSimpleCounterApi().getWordCount(key, TimeUnitEnum.MINUTE, DEFAULT_LIMIT_SLIDWIN_MIN);
        if (repeatValue < DEFAULT_LIMIT_COUNT_PERMIN) {
            return false;
        }
        logger.info("key {} is limit with{} at {}/min", key, repeatValue, DEFAULT_LIMIT_SLIDWIN_MIN);
        return true;
    }

    @Override
    public void addTick(BcpAlertConfigEntity alertConfigEntity) {
        String key = getRepeatKey(alertConfigEntity);
        if (StringUtils.isEmpty(key)) {
            return;
        }
        getSimpleCounterApi().add(key, String.valueOf(System.currentTimeMillis()), TimeUnitEnum.MINUTE, DEFAULT_LIMIT_SLIDWIN_MIN);
    }

}
