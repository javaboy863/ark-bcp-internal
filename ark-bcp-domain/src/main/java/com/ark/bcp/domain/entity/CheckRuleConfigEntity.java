package com.ark.bcp.domain.entity;

import com.ark.bcp.domain.vo.PageValueObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


/**
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CheckRuleConfigEntity extends PageValueObject implements Serializable {

    private static final long serialVersionUID = 1213079858996647673L;

    /**
     * 编号，主键
     **/
    private Long id;
    /**
     * 所属事件id
     **/
    private Long eventId;
    /**
     * 规则名称
     **/
    private String ruleName;

    /**
     * app编码
     */
    private String appCode;
    /**
     * 描述
     **/
    private String description;

    /**
     * Mock数据
     */
    private String mockData;
    /**
     * 状态，1-开, 0-关
     **/
    private Integer status;
    /**
     * 是否可用,0,正常，1：删除 是已经删除
     **/
    private Integer isDelete;
    /**
     * data version
     **/
    private Integer version;
    /**
     * 最终修改人
     **/
    private String updatedBy;
    /**
     * 最终修改人
     **/
    private String createdBy;
    /**
     * 创建时间
     **/
    private Date createdTime;
    /**
     * 修改时间
     **/
    private Date updatedTime;

    public Boolean isEnable() {
        return null != status && 1 == status;
    }

    public Boolean isDelayEvent() {
        return false;
    }

    public Boolean isZhaiYao() {
        return false;
    }
}
