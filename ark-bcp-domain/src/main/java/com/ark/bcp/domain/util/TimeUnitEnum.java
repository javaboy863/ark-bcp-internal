package com.ark.bcp.domain.util;

/**
 * ttl单位.
 *
 * @author lijm
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum TimeUnitEnum {
    HOUR(1, "时", 60 * 60),
    MINUTE(2, "分", 60),
    SECOND(3, "秒", 1),
    DAY(4, "天", 60 * 60 * 24),
    WEEK(5, "周", 60 * 60 * 24 * 7),
    MONTH(6, "月", 60 * 60 * 24 * 30);

    private int code;
    private String unit;
    private int unitSecond;

    TimeUnitEnum(int code, String unit, int unitSecond) {
        this.code = code;
        this.unit = unit;
        this.unitSecond = unitSecond;
    }

    public static TimeUnitEnum getByCode(int code) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.code == code) {
                return timeUnitEnum;
            }
        }
        return null;
    }

    public int getCode() {
        return this.code;
    }

    public String getUnit() {
        return this.unit;
    }

    public int getUnitSecond() {
        return this.unitSecond;
    }
}
