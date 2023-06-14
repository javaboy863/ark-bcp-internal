

package com.ark.bcp.domain.engine.frame.rss.domain;



import com.ark.bcp.domain.engine.frame.ComponentTypeEnum;

import java.io.Serializable;

/**
 */
public class PublishParams implements Serializable {
    private static final long serialVersionUID = -1746929740798818937L;
    private ComponentTypeEnum componentTypeEnum;
    private ActionType action;
    private String typeId;
    private Integer version;
    private Long time;

    /**
     * 组建操作类型.
     */
    @SuppressWarnings({"JavadocVariable", "AlibabaEnumConstantsMustHaveComment"})
    public enum ActionType {
        ADD("add"),
        DEL("delete"),
        UPDATE("update"),
        STATUS_ON("open"),
        STATUS_OFF("close");

        ActionType(String name) {
            this.name = name;
        }

        private String name;
    }

    public ComponentTypeEnum getComponentTypeEnum() {
        return componentTypeEnum;
    }

    public void setComponentTypeEnum(ComponentTypeEnum componentTypeEnum) {
        this.componentTypeEnum = componentTypeEnum;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
