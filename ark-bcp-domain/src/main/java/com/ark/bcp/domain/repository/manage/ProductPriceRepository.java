
package com.ark.bcp.domain.repository.manage;


import com.ark.bcp.domain.entity.ProductPriceEntity;

/**
 **/
public interface ProductPriceRepository {

    /**
     * 商品价时间点查询,按照时间倒排，时间最大的一个
     *
     * @param entity
     * @return
     */
    ProductPriceEntity queryLastCostPricePoint(ProductPriceEntity entity);
}
