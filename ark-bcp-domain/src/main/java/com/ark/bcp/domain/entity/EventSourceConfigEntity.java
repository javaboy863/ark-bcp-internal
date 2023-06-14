
package com.ark.bcp.domain.entity;

import com.ark.bcp.domain.constant.EventSourceTypeEnum;
import com.ark.bcp.domain.vo.PageValueObject;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EventSourceConfigEntity extends PageValueObject implements Serializable {
    private static final long serialVersionUID = -2913503134798690506L;

    private Long id;

    /**
     * 事件源名称
     */
    private String name;

    /**
     * 事件源描述
     */
    private String description;

    /**
     * 事件源类型
     *
     * @see EventSourceTypeEnum
     */
    private Integer type;

    private Integer delayTypeCode;
    private String delayTypeParam;

    /**
     * 详细配置
     */
    private String detailConf;

    private String appCode;

    /**
     * 采样频率，1-100，表示1/100 - 100/100
     */
    private Integer sampleRatio;

    /**
     * 触发方式，0 被动触发（业务检查），1 主动触发（巡检）
     * @EventTriggerTypeEnum
     */
    private Integer triggerType;

    private Integer status;

    private Integer version;

    private String createUser;

    private Date createTime;

    private String updateUser;

    private Date updateTime;

    private Integer isDelete;

    /**
     * 0 关闭， 1 开启
     */
    private EventMatchTemplateEntity matchTemplateEntity;

    public Boolean isEnable() {
        return null != this.getStatus() && 1 == this.getStatus() && 0 == this.getIsDelete();
    }

    @Override
    public String toString() {
        return "EventSourceConfigEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", detailConf='" + detailConf + '\'' +
                ", appCode='" + appCode + '\'' +
                ", sampleRatio=" + sampleRatio +
                ", status=" + status +
                ", version=" + version +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", updateUser='" + updateUser + '\'' +
                ", updateTime=" + updateTime +
                ", isDelete=" + isDelete +
                "} " + super.toString();
    }
}
