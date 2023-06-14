

package com.ark.bcp.domain.engine.frame.decision.execute;

/**
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum FinalDescEnum {
    ALL(-1, "all", "全部"),
    PASS(0, "pass", "通过"),
    REVIEW(1, "review", "人工审核"),
    REJECT(2, "reject", "拒绝");

    FinalDescEnum(int code, String desc, String dispalyName) {
        this.code = code;
        this.desc = desc;
        this.dispalyName = dispalyName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String getDispalyName() {
        return dispalyName;
    }

    public void setDispalyName(String dispalyName) {
        this.dispalyName = dispalyName;
    }

    private int code;
    private String desc;
    private String dispalyName;

    /**
     * 兑换枚举.
     *
     * @param codeValue ""
     * @return ""
     */
    public static FinalDescEnum fromCode(int codeValue) {
        for (FinalDescEnum finalDescEnum : values()) {
            if (finalDescEnum.code == codeValue) {
                return finalDescEnum;
            }
        }
        return null;
    }

    /**
     * 兑换枚举.
     *
     * @param descValue ""
     * @return ""
     */
    public static FinalDescEnum fromDesc(String descValue) {
        for (FinalDescEnum finalDescEnum : values()) {
            if (finalDescEnum.desc.equals(descValue)) {
                return finalDescEnum;
            }
        }
        return null;
    }
}
