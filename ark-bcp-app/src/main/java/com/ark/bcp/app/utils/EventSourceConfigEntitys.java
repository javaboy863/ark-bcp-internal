
package com.ark.bcp.app.utils;

import com.missfresh.risk.bcp.domain.entity.EventSourceConfigEntity;
import com.missfresh.risk.bcp.dto.EventSourceConfigDto;
import com.missfresh.risk.bcp.dto.InspectionEventSourceConfigDto;

/**
 */
public class EventSourceConfigEntitys {
    public static EventSourceConfigEntity queryParamFromInspectionEventDto(final InspectionEventSourceConfigDto configDto) {
        return EventSourceConfigEntity.builder()
                .isDelete(0)
                .status(configDto.getStatus())
                .id(configDto.getId())
                .name(configDto.getName())
                .triggerType(1)
                .build();
    }

    public static EventSourceConfigEntity queryParamFromEventSrouceConfigDto(final EventSourceConfigDto eventSourceConfigDto) {
        return EventSourceConfigEntity.builder()
                .type(eventSourceConfigDto.getType())
                .isDelete(0)
                .status(eventSourceConfigDto.getStatus())
                .id(eventSourceConfigDto.getId())
                .name(eventSourceConfigDto.getName())
                .appCode(eventSourceConfigDto.getAppCode())
                .triggerType(0)
                .build();
    }
}
