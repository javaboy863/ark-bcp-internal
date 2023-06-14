package com.ark.bcp.domain.constant;

/**
 */
public enum EventSourceTypeEnum {
    /**
     * otter.
     */
    OTTER(1, "otter"),
    /**
     * databus.
     */
    DATABUS(2, "databus"),
    /**
     * rocketmq.
     */
    ROCKETMQ(3, "rocketmq"),
    /**
     * kafka.
     */
    KAFKA(4, "kafka"),
    /**
     * http请求上报.
     */
    HTTP(5, "http请求上报"),
    /**
     * 业务日志.
     */
    LOG(6, "业务日志");

    private final int type;
    private final String desc;

    EventSourceTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static EventSourceTypeEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }

        for (EventSourceTypeEnum item : EventSourceTypeEnum.values()) {
            if (item.type == type) {
                return item;
            }
        }
        return null;
    }
}
