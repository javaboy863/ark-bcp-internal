package com.ark.bcp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelayEventResourceConfigDO {
    /**
     * 规定时间后执行,单位分钟.
     */
    private Integer delayxMinValue;
    /**
     * 定点执行的时间 hh:mm:ss.
     */
    private String delayAtValue;

}
