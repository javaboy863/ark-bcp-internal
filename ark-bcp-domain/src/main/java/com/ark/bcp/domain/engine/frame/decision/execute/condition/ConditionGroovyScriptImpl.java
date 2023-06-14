
package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.ExecuteResult;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import com.ark.bcp.domain.exception.ConditonConfigException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Method;

/**
 */
public class ConditionGroovyScriptImpl extends AbstractConditionBO {

    private static Logger logger = LoggerFactory.getLogger(ConditionGroovyScriptImpl.class);


    private volatile Class<?> groovyScriptClass = null;
    private volatile Method handleMethod = null;
    private final Object groovyLockObject = new Object();

    private volatile String loadMessage = null;

    public ConditionGroovyScriptImpl(ConditionConfigEntity conditionConfigEntity) {
        super(conditionConfigEntity);
        init(conditionConfigEntity);
    }

    private void verifyConditionParams() {
        if (null == this.getConditionConfigEntity()
                || !(this.getConditionConfigEntity().getParamsObject() instanceof BaseDynamicScriptParameter)) {
            throw new ConditonConfigException("配置异常");
        }
        if (null == groovyScriptClass) {
            throw new ConditonConfigException(StringUtils.isEmpty(loadMessage) ? "加载脚本失败" : loadMessage);
        }
        if (null == handleMethod) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("脚本中缺少对应方法:").append("\n");
            stringBuilder.append("public Object handle(").append("JSONObject ").append(") {\n");
            stringBuilder.append("}");
            throw new ConditonConfigException(stringBuilder.toString());
        }
    }

    public void init(ConditionConfigEntity entity) {
        if (entity == null) {
            return;
        }
        // 拆解参数
        BaseDynamicScriptParameter parameter = Conditions.parseConditionParameter(entity.getParams(), BaseDynamicScriptParameter.class);
        entity.setParamsObject(parameter);

        if (null != parameter) {
            // 根据左边值构建对应的左边readable对象
            String rawScriptSource = parameter.getRawScriptSource();
            logger.info("ConditionGroovyScriptImpl init groovy :{} ", parameter.getScriptId());
            if (logger.isDebugEnabled()) {
                logger.info(" groovy :{} load {}", parameter.getScriptId(), rawScriptSource);
            }
            try {
                synchronized (groovyLockObject) {
                    GroovyClassLoader classLoader = new GroovyClassLoader();
                    groovyScriptClass = classLoader.parseClass(rawScriptSource);
                    handleMethod = groovyScriptClass.getMethod("handle", JSONObject.class);
                }
            } catch (Exception e) {
                logger.info(" groovy :{} load error", parameter.getScriptId(), e);
                loadMessage = e.toString();
            }
        } else {
            logger.warn("ConditionSimpleImpl int error,{}", entity.getParams());
        }
    }

    @Override
    protected Object innerConditionInvoke(ContextWrap contextWrap) {
        Object rawParam = contextWrap.getRuntimeContext().get(Constant.RT_RAW_SCRIPT_PARAM);
        Object result = null;
        try {
            verifyConditionParams();

            GroovyObject groovyObject = (GroovyObject) groovyScriptClass.newInstance();
            result = handleMethod.invoke(groovyObject, rawParam);
            return scriptResultHandle(result);
        } catch (ConditonConfigException e) {
            logger.info("ConditionSimpleImpl handleMethod.invoke ConditonConfigException:"+JSON.toJSONString(e));
            return ExecuteResult.exception(e.getMessage());
        } catch (Exception e) {
            logger.info("ConditionSimpleImpl handleMethod.invoke error:"+JSON.toJSONString(e));
            return ExecuteResult.exception("未知错误:" + e.toString());
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("决策结果:{}, 入参:{}", JSON.toJSONString(result), JSON.toJSONString(contextWrap));
            }
        }
    }
}

