
package com.ark.bcp.domain.util;

import com.ark.bcp.domain.entity.DynamicCodeConfigEntity;
import com.ark.bcp.domain.entity.InspectionEventSourceEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.missfresh.risk.bcp.enums.LoadDataStrategyDefine;

/**
 */
public class DynamicCodeConfigEntitys {
    public static DynamicCodeConfigEntity transFromInspectionEventEntity(final InspectionEventSourceEntity iescEntity) {
        Long scriptId = iescEntity.getLoadDataGroovyScriptVO().getDynamicCodeConfigId();
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(iescEntity.getLoadDataStrategy());
        if (null == loadDataStrategyDefine) {
            throw new FailfastException(null, "未知策略类型:" + iescEntity.getLoadDataStrategy());
        }
        return DynamicCodeConfigEntity.builder()
                .id(scriptId)
                .conditionId(0L)
                .eventId(iescEntity.getId())
                .type(2)
                .name(loadDataStrategyDefine.name() + iescEntity.getName())
                .scriptContent(iescEntity.getLoadDataGroovyScriptVO().getDynamicCodeSrc())
                .createdBy(iescEntity.getCreateUser())
                .updatedBy(iescEntity.getUpdateUser())
                .build();
    }
}
