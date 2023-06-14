
package com.ark.bcp.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;


/**
 **/
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class BcpCheckRuleAlertEntity implements Serializable {

    private static final long serialVersionUID = 7685739285716333597L;
    /**
     * 编号，主键
     **/
    private Long id;
    /**
     * 所规则id
     **/
    private Long ruleId;
    /**
     * 报警类型
     **/
    private Integer alertType;
    /**
     * appcode
     */
    private String appCode;
    /**
     * 报警参数
     **/
    private String alertConfigJson;
    /**
     * 报警文案格式化
     */
    private String alertTextFormat;
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
