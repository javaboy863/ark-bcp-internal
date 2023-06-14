package com.ark.bcp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckRuleStatEntity {

    private Integer id;

    /**
     * 校验规则ID
     */
    private Integer ruleId;

    /**
     * 统计时间，可以表示不同的时间粒度
     */
    private Integer statTime;

    /**
     * 规则校验成功数
     */
    private Integer succCount;

    /**
     * 规则校验失败数（数据不一致）
     */
    private Integer failCount;

    /**
     * 规则校验异常数（脚本执行异常）
     */
    private Integer exceptionCount;

    private Date createTime;

    private Date updateTime;
}
