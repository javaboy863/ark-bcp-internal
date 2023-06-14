
package com.ark.bcp.domain.util;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author wangzheng@missfresh.cn on 2020-05-06
 */
public class TimeTrace {
    private long start = System.currentTimeMillis();
    private List<TimeTracePoint> tracePointList = Lists.newArrayList();

    public void addPoint(String ptName) {
        tracePointList.add(new TimeTracePoint(ptName));
    }

    /**
     * 获取追踪信息.
     *
     * @return ""
     */
    public String getTraceInfo() {
        if (CollectionUtils.isEmpty(tracePointList)) {
            return null;
        }
        StringBuilder traceInfo = new StringBuilder();
        for (TimeTracePoint timeTracePoint : tracePointList) {
            traceInfo.append(timeTracePoint.getPtName());
            traceInfo.append(":").append(timeTracePoint.getTimestamp() - start);
            traceInfo.append(",");
        }
        traceInfo.append("total:").append(System.currentTimeMillis() - start);
        return traceInfo.toString();
    }

    static class TimeTracePoint {
        private String ptName;
        private long timestamp;

        TimeTracePoint(String ptName) {
            this.ptName = ptName;
            timestamp = System.currentTimeMillis();
        }

        public String getPtName() {
            return ptName;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
