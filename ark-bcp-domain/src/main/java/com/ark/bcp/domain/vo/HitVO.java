
package com.ark.bcp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HitVO {
    /**
     * traceId
     */
    private String traceId;

    /**
     * hostName
     */
    private String hostName;

    /**
     * ip
     */
    private String ip;

    /**
     * index
     */
    private String index;

    /**
     * message
     */
    private String message;

    /**
     * type
     */
    private String type;

    /**
     * url
     */
    private String url;

    /**
     * logId
     */
    private String logId;

    /**
     * time
     */
    private String time;
}
