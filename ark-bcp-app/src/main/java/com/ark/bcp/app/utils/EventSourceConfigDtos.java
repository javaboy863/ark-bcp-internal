package com.ark.bcp.app.utils;

import com.missfresh.risk.bcp.dto.EventSourceConfigDto;
import com.missfresh.risk.bcp.enums.DelayTypeDefine;
import com.mryx.common.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 */

public class EventSourceConfigDtos {
    private static final int MAX_PERSENT = 100;
    private static final int MIN_PERSENT = 0;
    /**
     * 是否有效数据.
     *
     * @param configDto ""
     * @return ""
     */
    public static boolean isAvailable(EventSourceConfigDto configDto) {
        if (null == configDto || null == configDto.getId()) {
            return false;
        }
        return true;
    }

    /**
     * 是否开启.
     *
     * @param configDto ""
     * @return ""
     */
    public static boolean isEnable(EventSourceConfigDto configDto) {
        return null != configDto.getStatus() && 1 == configDto.getStatus() && 0 == configDto.getIsDelete();
    }

    /**
     * 是否采样执行.
     *
     * @param configDto ""
     * @return true 执行 false 不执行
     */
    public static boolean isSampleHandle(EventSourceConfigDto configDto) {
        if (null == configDto.getSampleRatio() || MAX_PERSENT <= configDto.getSampleRatio()) {
            return true;
        }

        Random random = new Random();
        int randomInt = random.nextInt(MAX_PERSENT);
        return randomInt <= configDto.getSampleRatio();
    }

    /**
     * 延迟执行的时间点.
     *
     * @param configDto ""
     * @return ""
     */
    public static Date delayTime(EventSourceConfigDto configDto) {
        final DelayTypeDefine delayTypeDefine = DelayTypeDefine.fromCode(configDto.getDelayTypeCode());
        if (DelayTypeDefine.DELAY_X_MIN == delayTypeDefine) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, configDto.getDelayxMinValue());
            return calendar.getTime();
        } else if (DelayTypeDefine.DELAY_AT == delayTypeDefine) {
            Date now = new Date();
            Date exeDate = DateUtil.parse(DateUtil.formatDate(now) + configDto.getDelayAtValue());
            if (exeDate.after(now)) {
                return exeDate;
            }
            return null;
        }
        return null;
    }
}
