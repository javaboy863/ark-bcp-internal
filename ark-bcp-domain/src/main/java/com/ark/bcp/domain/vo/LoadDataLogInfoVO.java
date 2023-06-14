package com.ark.bcp.domain.vo;

import com.missfresh.risk.bcp.dto.LogInfoDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoadDataLogInfoVO {
    /**
     * 日志解析配置信息
     */
    private List<LogInfoDetailDto> logInfoDetailDtos;

    /**
     * 距现在提前几分钟
     */
    private Integer beforeMinute;

    /**
     * 查询时间范围
     */
    private Integer timeRegion;
}
