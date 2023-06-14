

package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

/**
 */
public class ConditionGroupParameter {
    /**
     * 逻辑运算符
     **/
    private Integer logicOperator;

    public Integer getLogicOperator() {
        return logicOperator;
    }

    public void setLogicOperator(Integer logicOperator) {
        this.logicOperator = logicOperator;
    }

    public static ConditionGroupParameter newInstance(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            ConditionGroupParameter parameter = JSON.parseObject(str, ConditionGroupParameter.class);
            return parameter;
        } catch (Exception e) {
            return null;
        }
    }
}
