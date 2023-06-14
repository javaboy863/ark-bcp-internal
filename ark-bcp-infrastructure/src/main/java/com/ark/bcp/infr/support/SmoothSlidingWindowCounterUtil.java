

package com.ark.bcp.infr.support;


import com.missfresh.risk.bcp.domain.infrservice.SimpleCounterService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.util.TimeUnitEnum;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 */
public class SmoothSlidingWindowCounterUtil {
    public static long count(String key, int slidWidthSecend) {
        if (StringUtils.isEmpty(key) || slidWidthSecend <= 0) {
            return 0;
        }
        try {
            SimpleCounterService simpleCounterService = AbstractApplicationContextUtil.getExtension(SimpleCounterService.class, null);
            if (null == simpleCounterService) {
                return 0;
            }
            return simpleCounterService.getWordCount(key, TimeUnitEnum.SECOND, slidWidthSecend);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void add(String key, String member, long timpstamp, int slidWidthSecend) {
        if (StringUtils.isEmpty(key)
                || StringUtils.isEmpty(member)
                || timpstamp <= 0
                || slidWidthSecend <= 0) {
            return;
        }
        try {
            SimpleCounterService simpleCounterService = AbstractApplicationContextUtil.getExtension(SimpleCounterService.class, null);
            if (null == simpleCounterService) {
                return ;
            }
            simpleCounterService.add(key, member, timpstamp, TimeUnitEnum.SECOND, slidWidthSecend);
        } catch (Exception e) {
            // donothing
        }
    }

    public static List<String> members(String key, int slidWidthSecend) {
        if (StringUtils.isEmpty(key) || slidWidthSecend <= 0) {
            return null;
        }
        try {
            SimpleCounterService simpleCounterService = AbstractApplicationContextUtil.getExtension(SimpleCounterService.class, null);
            if (null == simpleCounterService) {
                return null;
            }
            return simpleCounterService.getWordCountValue(key, TimeUnitEnum.SECOND, slidWidthSecend);
        } catch (Exception e) {
            // donothing
        }
        return null;
    }
}
