package com.ark.bcp.domain.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 */
@Data
@Builder
public class EventMessageEntity<T> implements Serializable {
    private static final long serialVersionUID = 4527786302619218339L;
    /**
     * 消息的事件源ID
     */
    private Long dataSourceId;

    private String messageId;

    /**
     * 消息体，转成JSON对象
     */
    private JSONObject messageBody;

    /**
     * 消息的原始字符串
     */
    private String rawBody;

    /**
     * 转成业务对象
     */
    private T bizObject;

    /**
     * 消息接收时间
     */
    private Date receiveTime;
}
