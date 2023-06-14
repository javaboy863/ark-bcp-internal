

package com.ark.bcp.domain.engine.frame.decision.execute;


import lombok.*;

import java.io.Serializable;

/**
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RuleExcuteResult extends ExecuteResult implements Serializable {
    private static final long serialVersionUID = -8042038468671866369L;
    private String msg;
    private Long id;

    public static Boolean isHited(RuleExcuteResult result) {
        if (null != result
                && null != result.getReuslt()
                && result.getReuslt()
                && null != result.getAvailable()
                && result.getAvailable()
        ) {
            return true;
        }
        return false;
    }

    public static Boolean isFail(RuleExcuteResult result) {
        if (null != result
                && null != result.getAvailable()
                && !result.getAvailable()) {
            return true;
        }
        return false;
    }
}
