

package com.ark.bcp.domain.entity;

import com.ark.bcp.domain.vo.cron.CronStrategyVO;
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
public class CronFieldEntity {
    private CronStrategyVO strategy;
    private Integer rangeFrom;
    private Integer rangeTo;
    private Integer repeatFrom;
    private Integer repeatTick;
    private List<Integer> enums;
}
