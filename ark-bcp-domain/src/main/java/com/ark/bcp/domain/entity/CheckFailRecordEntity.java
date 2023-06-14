package com.ark.bcp.domain.entity;

import com.ark.bcp.domain.vo.PageValueObject;
import com.ark.bcp.domain.util.ZipCompressUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckFailRecordEntity extends PageValueObject {
    private Long id;

    /**
     * 校验规则ID
     */
    private Long ruleId;

    private String messageId;

    private String eventMessage;

    /**
     * 校验失败原因
     */
    private String reason;

    private String handleMsg;

    private Date createTime;

    private Date updateTime;

    private Integer status;

    public void zipEventMessage() {
        if (!StringUtils.isEmpty(eventMessage)) {
            try {
                eventMessage = ZipCompressUtils.gzip(eventMessage);
            } catch (Exception e) {
                // donothing
            }
        }
    }
    public void unzipEventMessage() {
        if (!StringUtils.isEmpty(eventMessage)) {
            try {
                eventMessage = ZipCompressUtils.gunzip(eventMessage);
            } catch (Exception e) {
                // donothing
            }
        }
    }
}
