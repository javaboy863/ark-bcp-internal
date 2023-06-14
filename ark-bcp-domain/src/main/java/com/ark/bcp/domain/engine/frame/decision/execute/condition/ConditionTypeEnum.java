
package com.ark.bcp.domain.engine.frame.decision.execute.condition;

/**
 * 条件类型.
 *
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ConditionTypeEnum {
    CONDITION(1, "条件"),
    CONDITION_SET(2, "条件组"),
    RULE_TEMPLATE(3, "规则模板"),
    JAVA_SCRIPT_CONDITION(4, "Java规则脚本"),
    GROORY_SCRIPT_CONDITION(5, "groory规则脚本"),
    QLEXPRESS_CONDITION(6, "QL表达式");

    private int code;
    private String type;

    ConditionTypeEnum(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public static ConditionTypeEnum getByCode(int code) {
        for (ConditionTypeEnum ctDto : ConditionTypeEnum.values()) {
            if (ctDto.code == code) {
                return ctDto;
            }
        }
        return null;
    }

    public int getCode() {
        return this.code;
    }

    public String getType() {
        return this.type;
    }
}
