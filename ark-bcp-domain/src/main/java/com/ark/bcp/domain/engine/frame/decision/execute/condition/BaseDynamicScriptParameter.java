

package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

/**
 */
public class BaseDynamicScriptParameter {
    private String scriptId;

    private String rawScriptSource;

    public String getRawScriptSource() {
        return rawScriptSource;
    }

    public void setRawScriptSource(String rawScriptSource) {
        this.rawScriptSource = rawScriptSource;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public static BaseDynamicScriptParameter newInstance(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            BaseDynamicScriptParameter parameter = JSON.parseObject(str, BaseDynamicScriptParameter.class);
            return parameter;
        } catch (Exception e) {
            return null;
        }
    }
}
