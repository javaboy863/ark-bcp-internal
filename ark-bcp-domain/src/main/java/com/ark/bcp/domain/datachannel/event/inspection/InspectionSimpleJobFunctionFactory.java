

package com.ark.bcp.domain.datachannel.event.inspection;

import com.ark.bcp.domain.datachannel.channel.EventMessageListenner;
import com.ark.bcp.domain.entity.InspectionEventSourceEntity;
import com.missfresh.risk.bcp.enums.LoadDataStrategyDefine;

/**
 */
public class InspectionSimpleJobFunctionFactory {
    public static InspectionSimpleJobFunction create(final LoadDataStrategyDefine loadDataStrategyDefine,
                                              final InspectionEventSourceEntity inspectionEventSourceEntity,
                                              final EventMessageListenner eventMessageListenner) {
        if (null == loadDataStrategyDefine || null == inspectionEventSourceEntity || null == eventMessageListenner) {
            return null;
        }
        switch (loadDataStrategyDefine) {
            case GROOVY_SCRIPT: {
                if (null != inspectionEventSourceEntity.getLoadDataGroovyScriptVO()) {
                    return new GroovyScriptInspectionJobFunction(inspectionEventSourceEntity.getLoadDataGroovyScriptVO(), eventMessageListenner);
                }
            }
            case TEMPLATE_MYSQL: {
                if (null != inspectionEventSourceEntity.getLoadDataTemplateVOList()) {
                    return new TemplateInspectionJobFunction(inspectionEventSourceEntity.getLoadDataTemplateVOList(), eventMessageListenner);
                }
            }
            case LOG_TYPE: {
                if (null != inspectionEventSourceEntity.getLoadDataLogInfoVo()) {
                    return new LogTypeInspectionJobFunction(inspectionEventSourceEntity.getLoadDataLogInfoVo(), eventMessageListenner);
                }
            }
            default: {

            }
        }
        return null;
    }
}
