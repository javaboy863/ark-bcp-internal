

package com.ark.bcp.domain.datachannel.event.inspection;

import io.elasticjob.lite.api.simple.SimpleJob;

/**
 */
public interface InspectionSimpleJobFunction extends SimpleJob {
    /**
     * 巡检关闭
     */
    void close();
}
