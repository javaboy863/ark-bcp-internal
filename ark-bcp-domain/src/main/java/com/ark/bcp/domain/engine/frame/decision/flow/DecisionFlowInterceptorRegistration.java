

package com.ark.bcp.domain.engine.frame.decision.flow;

/**
 */
public class DecisionFlowInterceptorRegistration {
    private DecisionFlowInterceptor decisionFlowInterceptor;

    private int order = 0;

    public DecisionFlowInterceptorRegistration(DecisionFlowInterceptor decisionFlowInterceptor) {
        this.decisionFlowInterceptor = decisionFlowInterceptor;
    }

    public DecisionFlowInterceptor getDecisionFlowInterceptor() {
        return decisionFlowInterceptor;
    }

    public void setDecisionFlowInterceptor(DecisionFlowInterceptor decisionFlowInterceptor) {
        this.decisionFlowInterceptor = decisionFlowInterceptor;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
