package com.missfresh.risk.bcp.web.script

import com.alibaba.fastjson.JSONObject
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.util.MapPathUtil
import org.springframework.util.StringUtils

class ItemPriceTooLowCheck {
    private static final Logger logger = LoggerFactory.getLogger(ItemPriceTooLowCheck.class);
    int threshold = 20;


    Map<String, Object> handle(JSONObject jsonObject) {
        Map<String, Object> retData = new HashMap<>();
        retData.put("hited", false);
        retData.put("msg", "")

        try {
            Long orderType = MapPathUtil.parseLong("order.orderType", jsonObject);
            String popBusinessNo = MapPathUtil.parse("order.popBusinessNo", jsonObject)

            // 只看云超订单 80 云超
            if (null == orderType || orderType != 80 || StringUtils.isEmpty(popBusinessNo)) {
                return retData;
            }
            // 非京东 42314 忽略
            if (!"42314".equals(popBusinessNo)) {
                return retData;
            }
            String orderItemSku = MapPathUtil.parse("orderItem.sku", jsonObject)

            // payFee*100/ purchasePrice * quantity
            Long payFee = MapPathUtil.parseLong("orderItem.payFee", jsonObject)
            Long purchasePrice = MapPathUtil.parseLong("orderItem.purchasePrice", jsonObject)
            Long quantity = MapPathUtil.parseLong("orderItem.quantity", jsonObject)

            if (null == payFee || null == purchasePrice || null == quantity) {
                return retData;
            }
            int tackoff = (payFee * 100) / (purchasePrice * quantity);
            logger.info("sku定价低于成本价 {} %:{},sku:{}", threshold, tackoff, orderItemSku);

            if (tackoff < threshold) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("sku: ").append(orderItemSku).append("\n");
                stringBuilder.append("skuName: ").append(MapPathUtil.parse("orderItem.productName", jsonObject)).append("\n")
                stringBuilder.append("实际支付: ").append(payFee).append("\n");
                stringBuilder.append("sku成本: ").append(purchasePrice).append("\n");
                stringBuilder.append("sku数量: ").append(quantity).append("\n");
                stringBuilder.append("折扣度: ").append(tackoff).append("\n");
                retData.put("msg", stringBuilder.toString())
                retData.put("hited", true)
                logger.info("sku定价低于成本价，报警 {} %:{},sku:{}", threshold, tackoff, orderItemSku);
                return retData;
            }
        } catch (Exception e) {
            logger.info("云超:京东，1分钟内，sku售卖数监测异常{}", jsonObject.toJSONString(), e);
        }
        return retData;
    }
}