
package com.ark.bcp.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;


/**
 **/
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BcpCheckRuleRepairEntity implements Serializable {

    private static final long serialVersionUID = 2296470749619193104L;

    /**
     * 编号，主键
     **/
    private Long id;
    /**
     * 所规则id
     **/
    private Long ruleId;
    /**
     * 修复类型
     **/
    private Integer repairType;
    /**
     * 修复配置参数
     **/
    private String repairConfigJson;
    /**
     * 是否压缩存储，1压缩，0未压缩
     **/
    private Integer zipped;
    /**
     * 创建时间
     **/
    private Date createTime;
    /**
     * 更新时间
     **/
    private Date updateTime;
}
