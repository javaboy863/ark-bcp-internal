package com.ark.bcp.domain.constant;

/**
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum EventTriggerTypeEnum {
    LISTEN(0, "ListenerEvent"),
    INSPECTION(1, "InpectionEvent");

    private final int type;
    private final String desc;

    EventTriggerTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static EventTriggerTypeEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }

        for (EventTriggerTypeEnum item : EventTriggerTypeEnum.values()) {
            if (item.type == type) {
                return item;
            }
        }
        return null;
    }
}
