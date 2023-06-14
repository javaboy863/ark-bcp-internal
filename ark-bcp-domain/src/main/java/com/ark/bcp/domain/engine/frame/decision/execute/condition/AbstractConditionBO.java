
package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.google.common.collect.Lists;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.ExecuteResult;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import com.ark.bcp.domain.util.MapPathUtil;

import java.util.Map;

/**
 * 描述类的功能.
 *
 */
public abstract class AbstractConditionBO {

    private ConditionConfigEntity conditionConfigEntity;

    public AbstractConditionBO(ConditionConfigEntity conditionConfigEntity) {
        this.conditionConfigEntity = conditionConfigEntity;
    }

    public ConditionConfigEntity getConditionConfigEntity() {
        return conditionConfigEntity;
    }

    public ExecuteResult conditionInvoke(final ContextWrap contextWrap) {
        Object invokeResultObject = innerConditionInvoke(contextWrap);
        return scriptResultHandle(invokeResultObject);
    }

    /**
     * 内部条件执行
     *
     * @param contextWrap
     * @return
     */
    protected abstract Object innerConditionInvoke(final ContextWrap contextWrap);

    protected ExecuteResult scriptResultHandle(Object result) {
        if (result instanceof ExecuteResult) {
            return (ExecuteResult) result;
        }
        if (result instanceof Map) {
            Boolean hited = MapPathUtil.parseBoolean("hited", (Map<String, Object>) result);
            String promotMsg = MapPathUtil.parse("msg", (Map<String, Object>) result);
            if (null == hited) {
                return ExecuteResult.exception("非法的返回格式。\n请返回Map，例如:{\"hited\":true,\"msg\":\"xxx is hited\"}");
            }
            return hited ? ExecuteResult.hited(promotMsg) : ExecuteResult.notHited();
        } else if (result instanceof Boolean) {
            return (Boolean) result ? ExecuteResult.hited(Lists.newArrayList()) : ExecuteResult.notHited();
        } else {
            // 没有拿到想要的结果格式
            return ExecuteResult.exception("非法的返回格式。\n请返回 true/false or {\"hited\":true,\"msg\":\"xxx is hited\"} ");
        }
    }
}
