

package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 */
public class Conditions {
    private static boolean availableLongValue(Long longVal) {
        return null != longVal && 0 != longVal;
    }

    public static List<ConditionConfigEntity> filterTopConditionSet(List<ConditionConfigEntity> conditions) {
        List<ConditionConfigEntity> subConditinos = Lists.newArrayList();
        if (null == conditions) {
            return subConditinos;
        }
        for (ConditionConfigEntity condition : conditions) {
            // 00
            if (ConditionTypeEnum.CONDITION_SET.getCode() == condition.getType() && !availableLongValue(condition.getParentId())) {
                subConditinos.add(condition);
            }
        }
        return subConditinos;
    }


    public static List<ConditionConfigEntity> filterConditions(List<ConditionConfigEntity> conditions, Long targetParentId) {
        List<ConditionConfigEntity> subConditinos = Lists.newArrayList();
        if (null == conditions) {
            return subConditinos;
        }
        for (ConditionConfigEntity condition : conditions) {
            // 00
            if (!availableLongValue(targetParentId) && !availableLongValue(condition.getParentId())) {
                if (ConditionTypeEnum.CONDITION_SET.getCode() != condition.getType()) {
                    subConditinos.add(condition);
                    continue;
                }
            }
            // 11
            if (availableLongValue(targetParentId) && availableLongValue(condition.getParentId())) {
                if (!condition.getParentId().equals(targetParentId)) {
                    continue;
                }
                subConditinos.add(condition);
            }
            // 01/10 均为不匹配类型
        }
        return subConditinos;
    }


    public static <T> T parseConditionParameter(String params, Class<T> clazz) {
        T paramObject = null;
        if (StringUtils.isEmpty(params)) {
            paramObject = null;
        }
        paramObject = JSON.parseObject(params, clazz);
        return paramObject;
    }

}
