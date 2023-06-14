

package com.ark.bcp.infr.cache.product;

import com.missfresh.risk.bcp.domain.entity.ProductPriceEntity;
import com.missfresh.risk.bcp.domain.infrservice.ProductPriceCacheService;
import com.missfresh.shardingredis.command.Sedis;
import com.mryx.common.utils.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Random;

/**
 */
@Component
public class ProductPriceCacheServiceImpl implements ProductPriceCacheService {

    private static final Logger logger = LoggerFactory.getLogger(ProductPriceCacheServiceImpl.class);

    @Resource(name = "product_price_sedis")
    private Sedis productCacheSedis;

    private static final Integer SEC_ONE_DAY = 24 * 60 * 60;


    private static String getProductCostPirceCacheKey(Integer regionCode, String sku) {
        String curData = DateUtils.formatDate(new Date(), "yyyy_MM_dd");
        return String.format("str:bcp:pdt:cost_%s_%s_%s", regionCode, sku, curData);
    }

    /**
     * 散列1天～2天的过期时间，避免集中过期
     *
     * @return
     */
    private static Integer getHashedTtl() {
        Random random = new Random();
        return SEC_ONE_DAY + random.nextInt(SEC_ONE_DAY);
    }

    @Override
    public ProductPriceEntity getLastProductPrice(Integer regionCode, String sku) {
        if (null == regionCode || StringUtils.isEmpty(sku)) {
            return null;
        }
        String key = getProductCostPirceCacheKey(regionCode, sku);
        try {
            String price = productCacheSedis.get(key);
            if (org.springframework.util.StringUtils.isEmpty(price)) {
                return null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("读取{},结果:{}", key, price);
            }
            return ProductPriceEntity.builder()
                    .sku(sku)
                    .costPrice(Integer.valueOf(price))
                    .regionCode(regionCode)
                    .build();
        } catch (Exception e) {
            logger.error("读取缓存异常:key,{}",key, e);
        }
        return null;
    }

    @Override
    public void saveLastProductPrice(Integer regionCode, String sku, ProductPriceEntity entity) {
        if (null == entity || null == entity.getCostPrice()
                || null == regionCode || StringUtils.isEmpty(sku)) {
            return;
        }
        try {
            productCacheSedis.setex(getProductCostPirceCacheKey(regionCode, sku), getHashedTtl(), String.valueOf(entity.getCostPrice()));
        } catch (Exception e) {
            logger.error("写入缓存异常:", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(getProductCostPirceCacheKey(1, "abc"));
    }
}
