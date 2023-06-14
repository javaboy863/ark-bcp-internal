package com.missfresh.risk.bcp.web.script

import com.alibaba.fastjson.JSONObject
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.util.MapPathUtil
import com.missfresh.risk.bcp.infr.support.PrivateSedisStorageUtil
import com.missfresh.risk.bcp.infr.support.SmoothSlidingWindowCounterUtil
import com.ark.bcp.web.web.script.DynamicDubboInvoke
import org.springframework.util.StringUtils

class OrderAlertNoJDCheck {
    private static final Logger logger = LoggerFactory.getLogger(OrderAlertNoJDCheck.class);
    int threshold = 100;
    int slidwidth = 60;

    String makeKvKey(String orderItemSku, String orderid) {
        return "kv:sku_cnt_in_order_" + orderItemSku + "_" + orderid;
    }

    String makecnKey(String orderItemSku) {
        return "cnt:sku_attach_order_" + orderItemSku;
    }

    private String userPhone(String userId) {
        try {

            String interfaceName = "com.missfresh.user.service.IUserCenterService"
            String methodName = "getUser"

            String[] types = new String[2]
            types[0] = "java.lang.Long"
            types[1] = "com.missfresh.user.bean.UserQueryBean"

            Map<String, Object> userQueryBean = new HashMap<>()
            userQueryBean.put("businessLineEnum", "MRYX");
            userQueryBean.put("needBase", true);

            Object[] values = new Object[2]
            values[0] = Long.valueOf(userId)
            values[1] = userQueryBean

            Map<String, Object> ref = new HashMap<>()
            ref.put("interface", interfaceName)
            ref.put("version", "1.0")
            ref.put("group","imf-usercenter-group")

            Map<String, Object> userinfo = (Map<String, Object>) DynamicDubboInvoke.invokeDubboWithReferenceProperties(ref, methodName, types, values)
            return MapPathUtil.parse("userBase.mobile", userinfo);
        } catch (Exception e) {
            logger.error("调用风控名单异常", e)
        }
        return null;
    }

    private void countOrder(String orderItemSku, String orderid, Long timestamp, Long unitNum) {
        if (null == timestamp || null == unitNum) {
            return;
        }
        String key = makecnKey(orderItemSku)
        SmoothSlidingWindowCounterUtil.add(key, orderid, null == timestamp ? System.currentTimeMillis() : timestamp, slidwidth);
        String kvKey = makeKvKey(orderItemSku, orderid);
        PrivateSedisStorageUtil.set(kvKey, String.valueOf(unitNum), slidwidth)
    }

    private boolean isBussinissPartener(String userphone) {
        return invokeRiskDataNameList(userphone, 1050);
    }

    private boolean isIgnoredSku(String sku) {
        return invokeRiskDataNameList(sku, 1051);
    }

    private boolean invokeRiskDataNameList(String record, int nameListId) {
        String interfaceName = "com.missfresh.antispam.data.service.IDataRecordService"
        String methodName = "isRecordExistInCache"
        String[] types = new String[2]
        types[0] = "int"
        types[1] = "java.lang.String"

        Object[] values = new Object[2]
        values[0] = nameListId
        values[1] = record
        try {
            Boolean obj = (Boolean) DynamicDubboInvoke.invokeDubboSimplified(interfaceName, methodName, types, values)
            return null != obj && obj;
        } catch (Exception e) {
            logger.error("调用风控名单异常", e)
        }
        return false;
    }

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

            String buyerid = MapPathUtil.parse("order.userId", jsonObject)
            String orderItemSku = MapPathUtil.parse("orderItem.sku", jsonObject)
            Long quantity = MapPathUtil.parseLong("orderItem.quantity", jsonObject)
            String orderId = MapPathUtil.parse("order.orderId", jsonObject)

            if(isIgnoredSku(orderItemSku)) {
                logger.info("云超:售卖SKU豁免,userid:{},sku售卖数:{},sku:{}, orderid:{}", buyerid, quantity, orderItemSku, orderId);
                return retData;
            }

            String userphone = userPhone(buyerid)
            if (null != userphone && !userphone.isEmpty()) {
                if (isBussinissPartener(userphone)) {
                    logger.info("云超:售卖豁免,userid:{}, phoneid:{}，sku售卖数:{},sku:{}, orderid:{}", buyerid, userphone, quantity, orderItemSku, orderId);
                    return retData;
                }
            }

            // 京东 42314 忽略
            if ("42314".equals(popBusinessNo)) {
                return retData;
            }

            if (StringUtils.is(orderId) || StringUtils.is(orderItemSku)) {
                return retData;
            }
            // 优先积累商品数
            Long timestamp = MapPathUtil.parseLong("order.createdTime", jsonObject)
            countOrder(orderItemSku, orderId, timestamp, quantity);

            String key = makecnKey(orderItemSku)
            long cnt = SmoothSlidingWindowCounterUtil.count(key, slidwidth);
            if (0l == cnt) {
                return retData;
            }
            if (cnt > threshold) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("sku: ").append(orderItemSku).append("\n");
                stringBuilder.append("关联订单数: ").append(cnt).append("\n");
                retData.put("msg", stringBuilder.toString())
                retData.put("hited", true)
                return retData;
            } else {
                List<String> orders = SmoothSlidingWindowCounterUtil.members(key, slidwidth);
                if (null == orders || 0 == orders.size()) {
                    return retData;
                }
                int skuInt = 0;
                for (i in 0..<orders.size()) {
                    String kvKey = makeKvKey(orderItemSku, orders.get(i))
                    String strUnitNum = PrivateSedisStorageUtil.get(kvKey);
                    if (null != strUnitNum) {
                        skuInt += Integer.valueOf(strUnitNum)
                    }
                }
                logger.info("云超:非京东，1分钟内，sku售卖数:{},sku:{}", skuInt, orderItemSku);
                if (skuInt > threshold) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("sku: ").append(orderItemSku).append("\n");
                    stringBuilder.append("skuName: ").append(MapPathUtil.parse("orderItem.productName", jsonObject)).append("\n")
                    stringBuilder.append("sku总量: ").append(skuInt).append("\n");
                    stringBuilder.append("关联订单数: ").append(orders.size()).append("\n");
                    stringBuilder.append("关联订单: ").append(orders.join(",")).append("\n");
                    retData.put("msg", stringBuilder.toString())
                    retData.put("hited", true)
                    return retData;
                }
            }
        } catch (Exception e) {
            logger.info("云超:非京东，1分钟内，sku售卖数监测异常{}", jsonObject.toJSONString(), e);
        }
        return retData;
    }
}