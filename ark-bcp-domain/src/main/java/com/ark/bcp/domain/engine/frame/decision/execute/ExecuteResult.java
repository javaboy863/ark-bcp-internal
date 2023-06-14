

package com.ark.bcp.domain.engine.frame.decision.execute;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 */
public class ExecuteResult implements Serializable {
    private static final long serialVersionUID = 7052432559345502211L;
    private Boolean reuslt;
    private Boolean available;
    private List<String> promotMsgs = null;

    public Boolean getReuslt() {
        return reuslt;
    }

    public void setReuslt(Boolean reuslt) {
        this.reuslt = reuslt;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<String> getPromotMsgs() {
        return promotMsgs;
    }

    public void setPromotMsgs(List<String> promotMsgs) {
        this.promotMsgs = promotMsgs;
    }

    public static ExecuteResult notHited() {
        ExecuteResult result = new ExecuteResult();
        result.setAvailable(true);
        result.setReuslt(false);
        result.setPromotMsgs(null);
        return result;
    }

    public static ExecuteResult hited(String promotMsg) {
        ExecuteResult result = new ExecuteResult();
        result.setAvailable(true);
        result.setPromotMsgs(Lists.newArrayList(promotMsg));
        result.setReuslt(true);
        return result;
    }

    public static ExecuteResult hited(List<String> promotMsgs) {
        ExecuteResult result = new ExecuteResult();
        result.setAvailable(true);
        result.setPromotMsgs(promotMsgs);
        result.setReuslt(true);
        return result;
    }

    public static ExecuteResult exception(String promotMsg) {
        ExecuteResult result = new ExecuteResult();
        result.setAvailable(false);
        result.setPromotMsgs(Lists.newArrayList(promotMsg));
        return result;
    }

    public static ExecuteResult exception(List<String> promotMsgs) {
        ExecuteResult result = new ExecuteResult();
        result.setAvailable(false);
        result.setPromotMsgs(Lists.newArrayList(promotMsgs));
        return result;
    }
}
