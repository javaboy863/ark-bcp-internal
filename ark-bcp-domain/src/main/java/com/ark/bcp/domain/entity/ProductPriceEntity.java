package com.ark.bcp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceEntity {
    private String sku;

    private String regionName;

    private Integer regionCode;

    private Integer costPrice;

    private String ptDate;

    private Date createTime;
}
