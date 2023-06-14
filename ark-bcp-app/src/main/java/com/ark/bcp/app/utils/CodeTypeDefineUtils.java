package com.ark.bcp.app.utils;

import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.condition.ConditionTypeEnum;
import com.missfresh.risk.bcp.enums.CodeTypeDefine;


/**
 */
public class CodeTypeDefineUtils {
    public static ConditionTypeEnum toConditionType(final int codeType) {
        if (CodeTypeDefine.Grovvy.getCode().equals(codeType)) {
            return ConditionTypeEnum.GROORY_SCRIPT_CONDITION;
        }
        if (CodeTypeDefine.QLExpress.getCode().equals(codeType)) {
            return ConditionTypeEnum.QLEXPRESS_CONDITION;
        }
        return null;
    }

    public static CodeTypeDefine fromConditionType(final int conditionTypeCode) {
        final ConditionTypeEnum conditionTypeEnum = ConditionTypeEnum.getByCode(conditionTypeCode);
        if (ConditionTypeEnum.GROORY_SCRIPT_CONDITION == conditionTypeEnum) {
            return CodeTypeDefine.Grovvy;
        }
        if (ConditionTypeEnum.QLEXPRESS_CONDITION == conditionTypeEnum) {
            return CodeTypeDefine.QLExpress;
        }
        return null;
    }
}
