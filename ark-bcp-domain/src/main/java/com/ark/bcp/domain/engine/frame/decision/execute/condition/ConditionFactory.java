

package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.google.common.collect.Maps;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 */
public class ConditionFactory {
    private static Logger logger = LoggerFactory.getLogger(ConditionFactory.class);

    private interface TypedConditionFactory {
        /**
         * 创建条件执行BO
         *
         * @param entity
         * @param subConditions
         * @return
         */
        AbstractConditionBO createConditionBo(
                final ConditionConfigEntity entity, final List<ConditionConfigEntity> subConditions);

    }


    static Map<ConditionTypeEnum, TypedConditionFactory> conditionFactoryMap = Maps.newConcurrentMap();

    static {
        conditionFactoryMap.put(ConditionTypeEnum.CONDITION_SET, new ConditionSetFactory());
        conditionFactoryMap.put(ConditionTypeEnum.GROORY_SCRIPT_CONDITION, new GroovyConditonFactory());
        conditionFactoryMap.put(ConditionTypeEnum.QLEXPRESS_CONDITION, new QlexpressConditonFactory());
    }

    /**
     * @param entity     当前条件/条件组实体
     * @param conditions 所有条件配置
     * @return 条件BO
     */
    public static AbstractConditionBO createConditionBo(
            final ConditionConfigEntity entity, final List<ConditionConfigEntity> conditions) {
        final ConditionTypeEnum cte = ConditionTypeEnum.getByCode(entity.getType());
        if (null == cte || !conditionFactoryMap.containsKey(cte)) {
            logger.info("条件类型不识别:{}", entity.getType());
            return null;
        }
        TypedConditionFactory tcf = conditionFactoryMap.get(cte);
        return tcf.createConditionBo(entity, conditions);
    }


    private static class ConditionSetFactory implements TypedConditionFactory {

        @Override
        public AbstractConditionBO createConditionBo(final ConditionConfigEntity entity,
                                                     final List<ConditionConfigEntity> subConditions) {
            if (ConditionTypeEnum.CONDITION_SET.getCode() != entity.getType()) {
                return null;
            }
            ConditionSetImpl conditionSet = new ConditionSetImpl(entity, subConditions);
            return conditionSet;
        }
    }

    private static class GroovyConditonFactory implements TypedConditionFactory {

        @Override
        public AbstractConditionBO createConditionBo(ConditionConfigEntity entity, List<ConditionConfigEntity> subConditions) {
            if (ConditionTypeEnum.GROORY_SCRIPT_CONDITION.getCode() != entity.getType()) {
                return null;
            }
            return new ConditionGroovyScriptImpl(entity);
        }
    }

    private static class QlexpressConditonFactory implements TypedConditionFactory {

        @Override
        public AbstractConditionBO createConditionBo(ConditionConfigEntity entity, List<ConditionConfigEntity> subConditions) {
            if (ConditionTypeEnum.QLEXPRESS_CONDITION.getCode() != entity.getType()) {
                return null;
            }
            return new ConditionQlexpressScriptImpl(entity);
        }
    }
}
