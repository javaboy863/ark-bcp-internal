

package com.ark.bcp.domain.engine.frame.decision.execute;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.util.Constant;

import java.util.Map;

/**
 */
public class ContextWrap {
    private Map<String, String> context;
    private Map<String, Object> runtimeContext = Maps.newHashMap();

    public ContextWrap(Map<String, String> context) {
        this.context = context;
    }

    public ContextWrap() {
    }

    public String getEventId() {
        return FieldSetterReader.Reader.getRuntimeStringField(runtimeContext, Constant.RT_EVENETID);
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public Map<String, Object> getRuntimeContext() {
        return runtimeContext;
    }

    public void setRuntimeContext(Map<String, Object> runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
