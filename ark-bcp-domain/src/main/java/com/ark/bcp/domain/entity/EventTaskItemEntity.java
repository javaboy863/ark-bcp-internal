package com.ark.bcp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 事件被CheckRule接收以后，转成task并调度执行.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTaskItemEntity {
    /**
     * 最大分片数量.
     */
    public static final int MAX_SHARDING = 16;
    /**
     * 最大重试次数.
     */
    public static final int MAX_RETRY = 3;

    private Long id;

    private Integer sharding;

    /**
     * 所属的校验事件.
     */
    private Long eventId;

    /**
     * 所属的校验规则ID
     */
    private Long ruleId;

    /**
     * 消息ID，例如MQ的messageId、日志的traceUrl等
     */
    private String messageId;

    /**
     * 事件消息体
     */
    private String eventMessage;

    /**
     * task的超时时间，为延迟task准备，即时task该字段可以为null
     */
    private Date expireTime;

    /**
     * task的执行状态，0:未处理，1:处理中，2:已处理
     */
    private Integer status;

    private Integer retryTime;
    /**
     * 事件接收时间
     */
    private Date receiveTime;

    private Integer version;

    private Date createTime;

    private Date updateTime;

    private int maxPageSize;

    public boolean isMaxRetry() {
        if (null != retryTime && retryTime >= MAX_RETRY) {
            return true;
        }
        return false;
    }
}
