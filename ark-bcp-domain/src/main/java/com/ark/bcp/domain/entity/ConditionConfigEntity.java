
package com.ark.bcp.domain.entity;

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
public class ConditionConfigEntity implements Serializable {

    private static final long serialVersionUID = 5030168157592537717L;

    /**
     * 编号，主键
     **/
    private Long id;
    /**
     * 所属规则编号
     **/
    private Long ruleId;
    /**
     * 条件类型，条件、条件组、规则模板
     **/
    private Integer type;
    /**
     * 父条件id
     **/
    private Long parentId;
    /**
     * 如果是规则模板，值为模板的描述信息
     **/
    private String params;
    /**
     * 是否可用,0x1 是已经删除
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

    /**
     * 参数对象，无需序列化
     */
    private transient Object paramsObject;
}
