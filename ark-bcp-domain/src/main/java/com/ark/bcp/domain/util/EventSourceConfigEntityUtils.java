
package com.ark.bcp.domain.util;

import com.ark.bcp.domain.entity.EventSourceConfigEntity;

/**
 */
public class EventSourceConfigEntityUtils {
    public static String decisionMonitorId(EventSourceConfigEntity entity) {
        return "busi-decision-event_" + entity.getId();
    }

    public static String decisionMonitorName(EventSourceConfigEntity entity) {
        return "决策过程" + entity.getId();
    }

    public static String decisionMonitorId(Long eventid) {
        return "busi-decision-event_" + eventid;
    }

    public static String decisionMonitorName(Long eventid) {
        return "决策过程" + eventid;
    }

    public static String decisionMonitorId(String eventid) {
        return "busi-decision-event_" + eventid;
    }

    public static String decisionMonitorName(String eventid) {
        return "决策过程" + eventid;
    }
}
