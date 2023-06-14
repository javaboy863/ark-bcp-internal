
package com.ark.bcp.domain.vo.bizzvos;

/**
 * @author wangzheng@missfresh.cn on 2020/11/18
 */
@SuppressWarnings("all")
public enum BizzEnumType {
    RIK(1, "RISK"),
    ARK(2, "ARK"),
    USR(3, "USER"),
    REFUNDBUSINESSTYPE(4, "RefundBusinessType");

    BizzEnumType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;
    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    public static BizzEnumType transfromCode(Integer code) {
        for (BizzEnumType value : BizzEnumType.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}