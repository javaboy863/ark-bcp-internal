package com.missfresh.risk.bcp.web.script

import com.alibaba.fastjson.JSONObject
import com.missfresh.risk.bcp.domain.util.MapPathUtil
import com.missfresh.risk.bcp.infr.support.ProductPirceCostUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.mryx.monitor.api.MultiTagMonitor
import com.mryx.monitor.item.MultiTagItem

class SkuPriceTask {

    private static final Logger logger = LoggerFactory.getLogger("SkuPriceTask");

    private JSONObject productCoat(String sku, Integer reginonId) {
        try {
            JSONObject reg = ProductPirceCostUtil.productPriceByAreaId(sku, reginonId);
            return reg;
        } catch (Exception e) {
            logger.info("异常", e);
        }
        return null;
    }
    // 必须包含一个 此接口，否则执行不通过。
    boolean handle(JSONObject priceChangeSendMsgDO) {
        boolean result = false;
        try {
            String sku = priceChangeSendMsgDO.getString("sku");
            Integer regionId = priceChangeSendMsgDO.getInteger("regionId");
            Integer price = priceChangeSendMsgDO.getInteger("price");
            String operator = priceChangeSendMsgDO.getString("operator");

            if (null == operator || operator.isEmpty()) {
                MultiTagMonitor.record(MultiTagItem.build("busi-groovy_SkuPriceDemo", "Sku价格变更检查")
                        .addTag("result", "drop"));
                return result;
            }
            if (operator.contains("system")
                    || operator.contains("SYSTEM")
                    || operator.contains("SMART")
                    || operator.contains("smart")
                    || operator.contains("null")) {
                MultiTagMonitor.record(MultiTagItem.build("busi-groovy_SkuPriceDemo", "Sku价格变更检查")
                        .addTag("result", "drop"));
                return result;
            }

            // 获取定价
            JSONObject costPriceObj = productCoat(sku, regionId);
            Long costPrice = MapPathUtil.parseLong("costPrice", costPriceObj)
            if (null == costPrice) {
                logger.info("获取定价失败:sku {},region{}", sku, regionId);
                MultiTagMonitor.record(MultiTagItem.build("busi-groovy_SkuPriceDemo", "Sku价格变更检查")
                        .addTag("result", "nosku"));
                return result;
            }
            // 销售价 占 成本价 低于30 报警
            BigDecimal priceDecimal = new BigDecimal(100.0f * price);
            BigDecimal pricePersent = priceDecimal.div(costPrice);
            int pricePersentInt = pricePersent.intValue();
            logger.info("sku {} 定价: price:{} cost:{},op:{},per:{}", sku, price, costPrice, operator, pricePersentInt);

            if (pricePersentInt <= 30) {
                MultiTagMonitor.record(MultiTagItem.build("busi-groovy_SkuPriceDemo", "Sku价格变更检查")
                        .addTag("result", "alert"));
                logger.info("获取定价报警:sku {}", sku);
                result = false;
            } else {
                MultiTagMonitor.record(MultiTagItem.build("busi-groovy_SkuPriceDemo", "Sku价格变更检查")
                        .addTag("result", "nomarl"));
            }
        } catch (Exception e) {
            logger.info("比对价格异常", e);
            MultiTagMonitor.record(MultiTagItem.build("busi-groovy_SkuPriceDemo", "Sku价格变更检查")
                    .addTag("result", "Exception"));
        } finally {
            logger.info("比价结果:{}", JSONObject.toJSONString(result));
        }
        return result;
    }
}
