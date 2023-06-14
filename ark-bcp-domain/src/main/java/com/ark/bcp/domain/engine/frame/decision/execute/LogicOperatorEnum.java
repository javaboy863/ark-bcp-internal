package com.ark.bcp.domain.engine.frame.decision.execute;

/**
 * 逻辑运算符.
 *
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum LogicOperatorEnum {
    AND(1, "&&", "满足以下所有条件"),
    OR(2, "||", "满足以下任意条件"),
    ALL_NOT_MATCH(3, "and", "以下条件均不满足"),
    AT_LEAST_ONE_NOT_MATCH(4, "||", "以下条件至少一条不满足");

    private int code;
    private String logicOperator;
    private String displayName;

    LogicOperatorEnum(int code, String logicOperator, String displayName) {
        this.code = code;
        this.logicOperator = logicOperator;
        this.displayName = displayName;
    }

    public static LogicOperatorEnum getByCode(int code) {
        for (LogicOperatorEnum loDto : LogicOperatorEnum.values()) {
            if (loDto.code == code) {
                return loDto;
            }
        }
        return null;
    }

    public int getCode() {
        return this.code;
    }

    public String getLogicOperator() {
        return this.logicOperator;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
