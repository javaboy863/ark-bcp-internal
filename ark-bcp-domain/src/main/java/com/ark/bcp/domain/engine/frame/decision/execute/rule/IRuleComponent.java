

package com.ark.bcp.domain.engine.frame.decision.execute.rule;


import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.RuleExcuteResult;

/**
 */
public interface IRuleComponent {
    /**
     * 规则执行
     *
     * @param contexts
     * @return
     */
    RuleExcuteResult ruleExecute(ContextWrap contexts);
}
