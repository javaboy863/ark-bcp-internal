package com.ark.bcp.domain.constant;

/**
 */
public enum EventSourceStatusEnum {
    /**
     * 已删除.
     */
    DELETED(-1, "已删除"),
    /**
     * 关闭中.
     */
    CLOSE(0, "关闭中"),
    /**
     * 已开启.
     */
    OPEN(1, "已开启");

    private final int code;
    private final String desc;

    EventSourceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EventSourceStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (EventSourceStatusEnum item : EventSourceStatusEnum.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }
}

