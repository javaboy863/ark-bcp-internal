

package com.ark.bcp.domain.engine.frame.decision.execute;

/**
 * 策略集需要实现.
 */
public interface IDecisionContext {
    /**
     * 调用决策
     * @param contexts
     * @return
     */
    DecisionExcuteResult invoke(ContextWrap contexts);
}
