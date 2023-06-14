package com.ark.bcp.domain.constant;

/**
 */
public enum EventTaskStatusEnum {
    /**
     * 待处理.
     */
    PENDING(0, "待处理"),
    /**
     * 处理中.
     */
    PROCESSING(1, "处理中"),
    /**
     * 已处理.
     */
    FINISHED(2, "已处理");

    private final int code;
    private final String desc;

    EventTaskStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EventTaskStatusEnum fromCode(Integer code) {
        if(code == null) {
            return null;
        }

        for (EventTaskStatusEnum item : EventTaskStatusEnum.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }
}

