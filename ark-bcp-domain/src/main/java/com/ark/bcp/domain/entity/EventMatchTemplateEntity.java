
package com.ark.bcp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMatchTemplateEntity implements Serializable {

    private static final long serialVersionUID = 1518243736625662480L;

    private Long id;

    /**
     * 模版展示名.
     * templete_bizx_@productname@_@orderid@
     */
    private String templateName;
    /**
     * 全局唯一
     */
    private String templateKey;


    /**
     * 所属eventsourceId
     */
    private Long eventSourceId;

    private Integer saveToMatchDbFlag;

    private Date createTime;

    private Date updateTime;
}
