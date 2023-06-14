package com.ark.bcp.domain.vo;

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
public class LogResultVO {
    /**
     * total
     */
    private Integer total;

    /**
     * took
     */
    private Integer took;

    /**
     * scrollId
     */
    private String scrollId;

    /**
     * hits
     */
    private List<HitVO> hits;
}
