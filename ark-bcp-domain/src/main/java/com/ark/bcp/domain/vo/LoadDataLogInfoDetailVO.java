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
public class LoadDataLogInfoDetailVO {

    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 日志关键字
     */
    private String logKeyword;

    /**
     * 排序
     */
    private Integer sort;
}
