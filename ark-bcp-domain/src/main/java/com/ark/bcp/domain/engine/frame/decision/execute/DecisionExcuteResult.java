

package com.ark.bcp.domain.engine.frame.decision.execute;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.Map;

/**
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionExcuteResult implements Serializable {
    private static final long serialVersionUID = 817728528819323686L;
    private long expend;
    private int decision;
    private DecisionExcuteData data;

    /**
     * .
     *
     * @param result ""
     * @return ""
     */
    public static boolean isException(DecisionExcuteResult result) {
        if (null != result
                && null != result.getData()
                && !MapUtils.isEmpty(result.getData().getExceptioned())
        ) {
            return true;
        }
        return false;
    }

    public static boolean isPass(DecisionExcuteResult result) {
        if (null != result && FinalDescEnum.PASS.getCode() == result.getDecision()) {
            return true;
        }
        return false;
    }

    public static boolean isReject(DecisionExcuteResult result) {
        if (null != result && FinalDescEnum.REJECT.getCode() == result.getDecision()) {
            return true;
        }
        return false;
    }

    /**
     * data.
     */
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DecisionExcuteData implements Serializable {
        private static final long serialVersionUID = -3771854087158697283L;
        private Map<Long, RuleExcuteResult> hited;
        private Map<Long, RuleExcuteResult> exceptioned;
    }

}
