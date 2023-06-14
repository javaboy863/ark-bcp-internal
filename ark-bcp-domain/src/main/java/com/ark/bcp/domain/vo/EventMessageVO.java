package com.ark.bcp.domain.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessageVO {
    /**
     * 事件源名称
     */
    private String sourceName;

    /**
     * 事件的唯一ID
     */
    private String messageId;

    /**
     * 事件源Id
     */
    private Long sourceId;

    /**
     * 事件消息体
     */
    private String messageBody;
}
