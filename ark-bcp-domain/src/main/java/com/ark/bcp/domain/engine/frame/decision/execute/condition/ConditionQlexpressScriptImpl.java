
package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.infrservice.ExpressExecutorService;
import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.ExecuteResult;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ConditionQlexpressScriptImpl extends AbstractConditionBO {

    private static Logger logger = LoggerFactory.getLogger(ConditionQlexpressScriptImpl.class);

    public ConditionQlexpressScriptImpl(ConditionConfigEntity conditionConfigEntity) {
        super(conditionConfigEntity);
        init(conditionConfigEntity);
    }

    @Override
    protected Object innerConditionInvoke(ContextWrap contextWrap) {
        Object result = null;

        try {
            if (!verifyConditionParams()) {
                throw new FailfastException(null, "条件参数不足");
            }

            BaseDynamicScriptParameter parameter =
                    (BaseDynamicScriptParameter) this.getConditionConfigEntity().getParamsObject();
            Object rawParam = contextWrap.getRuntimeContext().get(Constant.RT_RAW_SCRIPT_PARAM);

            ExpressExecutorService expressExecutorService = AbstractApplicationContextUtil.getExtension(ExpressExecutorService.class, null);
            result = expressExecutorService.execute(parameter.getRawScriptSource(), rawParam);
        } catch (FailfastException e) {
            logger.info("快速失败:{}", e.getMessage());
            result = ExecuteResult.notHited();
        } catch (Exception e) {
            result = ExecuteResult.exception(e.getMessage());
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("决策结果:{}, 入参:{}", JSON.toJSONString(result), JSON.toJSONString(contextWrap));
            }
        }
        return result;
    }


    private Boolean verifyConditionParams() {
        if (null == this.getConditionConfigEntity()
                || !(this.getConditionConfigEntity().getParamsObject() instanceof BaseDynamicScriptParameter)) {
            return false;
        }

        return true;
    }

    private void init(ConditionConfigEntity entity) {
        if (null == entity) {
            return;
        }
        // 拆解参数
        BaseDynamicScriptParameter parameter = Conditions.parseConditionParameter(entity.getParams(), BaseDynamicScriptParameter.class);
        getConditionConfigEntity().setParamsObject(parameter);
    }

}

