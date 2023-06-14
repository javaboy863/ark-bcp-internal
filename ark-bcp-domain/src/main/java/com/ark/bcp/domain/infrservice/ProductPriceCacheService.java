package com.ark.bcp.domain.infrservice;

import com.ark.bcp.domain.entity.ProductPriceEntity;

/**
 */
public interface ProductPriceCacheService {
    /**
     * 获取最新的商品成本
     *
     * @param regionCode
     * @param sku
     * @return
     */
    ProductPriceEntity getLastProductPrice(Integer regionCode, String sku);

    /**
     * 保存最新的上你价格
     *
     * @param regionCode
     * @param sku
     * @param entity
     */
    void saveLastProductPrice(Integer regionCode, String sku, ProductPriceEntity entity);
}
