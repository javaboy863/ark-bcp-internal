package com.ark.bcp.domain.service;

import com.ark.bcp.domain.entity.ProductPriceEntity;
import com.ark.bcp.domain.infrservice.ProductPriceCacheService;
import com.ark.bcp.domain.repository.manage.ProductPriceRepository;
import com.ark.bcp.domain.vo.AreaSourceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 */
@Service(value = "productCostPriceService")
public class ProductCostPriceService {
    private static final Logger logger = LoggerFactory.getLogger(ProductCostPriceService.class);

    @Resource
    private ProductPriceRepository productPriceRepository;

    @Resource
    private ProductPriceCacheService productPriceCacheService;

    public ProductCostPriceService() {
    }

    /**
     * 查询时间点的产品成本
     *
     * @param sku
     * @param areaId
     * @param pointTime 目标时间点，保留字段
     * @return
     */
    public ProductPriceEntity queryProductPrice(
            final String sku,
            final Integer areaId,
            final Date pointTime
    ) {
        //  转换areaCode
        AreaSourceEnum areaSourceEnum = AreaSourceEnum.fromCode(areaId);
        if (null == areaSourceEnum) {
            logger.info("不识别的大区编码");
            return null;
        }
        ProductPriceEntity lastSku = null;
        lastSku = productPriceCacheService.getLastProductPrice(areaId, sku);
        if (null != lastSku) {
            return lastSku;
        }
        ProductPriceEntity query = ProductPriceEntity.builder()
                .sku(sku)
                .regionName(areaSourceEnum.getDesc())
                .build();
        lastSku = productPriceRepository.queryLastCostPricePoint(query);
        if (null != lastSku) {
            productPriceCacheService.saveLastProductPrice(areaSourceEnum.getCode(), sku, lastSku);
        }

        return lastSku;
    }
}
