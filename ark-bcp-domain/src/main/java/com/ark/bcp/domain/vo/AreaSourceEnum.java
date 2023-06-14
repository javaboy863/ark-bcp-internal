
package com.ark.bcp.domain.vo;

import java.io.Serializable;

/**
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum AreaSourceEnum implements Serializable {
    MRYXALL(0, "MRYXALL", "全国"),
    MRYXHB(1, "MRYXHB", "华北"),
    MRYXHD(2, "MRYXHD", "华东"),
    MRYXHN(3, "MRYXHN", "华南"),
    MRYXHZ(4, "MRYXHZ", "华中");

    private Integer code;
    private String name;
    private String desc;

    private AreaSourceEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public static AreaSourceEnum fromCode(Integer code) {
        for (AreaSourceEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}

