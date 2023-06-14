

package com.ark.bcp.infr.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.missfresh.risk.bcp.domain.entity.ProductPriceEntity;
import com.missfresh.risk.bcp.domain.service.ProductCostPriceService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.vo.AreaSourceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 */
public class ProductPirceCostUtil extends AbstractApplicationContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(ProductPirceCostUtil.class);

    public static JSONObject productPriceByAreaId(String productId, Integer areeaId) {
        ProductCostPriceService productCostPriceService = getExtension(ProductCostPriceService.class, "productCostPriceService");
        if (null == productCostPriceService) {
            return null;
        }
        ProductPriceEntity entity = productCostPriceService.queryProductPrice(productId, areeaId, new Date());
        return JSON.parseObject(JSON.toJSONString(entity));
    }

    public static JSONObject productPrice(String productId) {
        return productPriceByAreaId(productId, AreaSourceEnum.MRYXALL.getCode());
    }
}
