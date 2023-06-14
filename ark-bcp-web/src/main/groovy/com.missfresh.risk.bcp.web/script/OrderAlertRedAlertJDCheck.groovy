package com.missfresh.risk.bcp.web.script

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.common.base.Stopwatch
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.exception.FailfastException
import com.missfresh.risk.bcp.domain.exception.IllegalParamException
import com.missfresh.risk.bcp.domain.util.MapPathUtil
import com.missfresh.risk.bcp.infr.support.DecisionResultUtil
import com.missfresh.risk.bcp.infr.support.PrivateSedisStorageUtil
import com.missfresh.risk.bcp.infr.support.SmoothSlidingWindowCounterUtil
import com.missfresh.risk.bcp.infr.support.UserInfoUtil
import com.ark.bcp.web.web.script.DynamicDubboInvoke
import org.apache.commons.lang3.tuple.Pair
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

import java.util.concurrent.TimeUnit

class OrderAlertRedAlertJDCheck {
    private static final Logger logger = LoggerFactory.getLogger("OrderAlertRedAlertJDCheck");


    String makeHaskKey(String orderItemSku, String orderid) {
        return "hash:sku_cnt_in_order_" + orderItemSku + "_" + orderid;
    }

    String makeHighRiskLevelKey(String orderItemSku) {
        return "cnt:sku_attach_order_high_" + orderItemSku;
    }


    private void countHighOrder(String orderItemSku, String orderid, Long purchasePrice, Long payPrice, Long quantity, Long timestamp) {
        if (null == timestamp) {
            return;
        }
        // 最长5分钟
        int slidwidth = 60 * 5;
        String key = makeHighRiskLevelKey(orderItemSku)
        SmoothSlidingWindowCounterUtil.add(key, orderid, null == timestamp ? System.currentTimeMillis() : timestamp, slidwidth);
        int a = SmoothSlidingWindowCounterUtil.count(key, slidwidth);

        String haskKey = makeHaskKey(orderItemSku, orderid);
        Map<String, String> hashValues = new HashMap<>();
        hashValues.put("purchase", purchasePrice.toString());
        hashValues.put("pay", payPrice.toString());
        hashValues.put("quantity", quantity.toString());
        PrivateSedisStorageUtil.hmset(haskKey, hashValues, slidwidth)
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

    private void handleBussinessType(final JSONObject jsonObject) {
        Long orderType = MapPathUtil.parseLong("order.orderType", jsonObject);
        String popBusinessNo = MapPathUtil.parse("order.popBusinessNo", jsonObject)

        // 只看云超订单 80 云超
        if (null == orderType || orderType != 80 || StringUtils.isEmpty(popBusinessNo)) {
            throw new FailfastException(null, "非云超订单，跳过")
        }
        // 非京东 42314 忽略
        if (!"42314".equals(popBusinessNo)) {
            throw new FailfastException(null, "非京东商品，跳过")
        }
    }

    private void handleParamCheck(final JSONObject jsonObject) {
        String orderItemSku = MapPathUtil.parse("orderItem.sku", jsonObject)
        String orderId = MapPathUtil.parse("order.orderId", jsonObject)
        Long quantity = MapPathUtil.parseLong("orderItem.quantity", jsonObject)
        Long purchasePrice = MapPathUtil.parseLong("orderItem.purchasePrice", jsonObject)
        Long payPrice = MapPathUtil.parseLong("orderItem.payFee", jsonObject)
        // 60 为 鲜币支付
        if (null != MapPathUtil.parseLong("orderFeeSplits.60.price", jsonObject)) {
            payPrice += MapPathUtil.parseLong("orderFeeSplits.60.price", jsonObject);
        }
        Long timestamp = MapPathUtil.parseLong("order.createdTime", jsonObject)
        if (null == orderItemSku || null == orderId || null == quantity || null == purchasePrice || null == payPrice || null == timestamp) {
            throw new IllegalParamException("属性缺失:" + JSON.toJSONString(jsonObject));
        }
        if (0 == payPrice) {
            throw new FailfastException(null, "支付额度为0");
        }
    }

    private void handleIgnoreByNamedlist(final JSONObject jsonObject) {
        String buyerid = MapPathUtil.parse("order.userId", jsonObject)
        String orderItemSku = MapPathUtil.parse("orderItem.sku", jsonObject)

        if (isIgnoredSku(orderItemSku)) {
            logger.info("云超:售卖SKU豁免,userid:{},sku售卖数:{},sku:{}, orderid:{}", buyerid, quantity, orderItemSku, orderId);
            throw new FailfastException(null, "商品豁免");
        }

        String userphone = UserInfoUtil.usetRegistPhone(Long.valueOf(buyerid))
        logger.info("用户转手机号:,userid: {}, phoneid:{}", buyerid, userphone);

        if (null != userphone && !userphone.isEmpty()) {
            if (isBussinissPartener(userphone)) {
                logger.info("云超:售卖豁免,userid:{}, phoneid:{}，sku售卖数:{},sku:{}, orderid:{}", buyerid, userphone, quantity, orderItemSku, orderId);
                throw new FailfastException(null, "用户");
            }
        }
    }

    private int handleMiddlePayRate(final Long payPrice, final Long purchasePrice) {
        BigDecimal payDecimal = new BigDecimal(payPrice);
        BigDecimal rate = (payDecimal - purchasePrice) * 100 / payDecimal;
        return rate.intValue();
    }

    private int handleRiskLogicPropertiesSumByCounter(final String orderItemSku, final String key, final String subkey, final int slidwidth) {
        List<String> orders = SmoothSlidingWindowCounterUtil.members(key, slidwidth);
        int skuInt = 0;
        if (null == orders || 0 == orders.size()) {
            return skuInt; // 找不到详情
        }
        for (i in 0..<orders.size()) {
            String hashKey = makeHaskKey(orderItemSku, orders.get(i))
            String quantity = PrivateSedisStorageUtil.hget(hashKey, subkey);
            if (null != quantity && !quantity.isEmpty()) {
                skuInt += Integer.valueOf(quantity)
            }
        }
        return skuInt;
    }

    private void handleAttachBindedOrderContext(final String key, final int slidwidth, List<Pair<String, Object>> alertContext) {
        List<String> orders = SmoothSlidingWindowCounterUtil.members(key, slidwidth);
        if (!CollectionUtils.isEmpty(orders)) {
            alertContext.add(Pair.of("关联订单数", orders.size()))
            alertContext.add(Pair.of("关联订单", orders.join(",")))
        }
    }

    private boolean handleRiskLogicByCounter(final String key, final String orderItemSku, final int slidwidth, final int threshold, List<Pair<String, Object>> alertContext) {
        // sku 关联的订单数 cnt
        long cnt = SmoothSlidingWindowCounterUtil.count(key, slidwidth);
        if (0l == cnt) {
            return false;
        }
        if (cnt > threshold) {
            // cnt 直接超过阈值，不必计算了
            alertContext.add(Pair.of("sku", orderItemSku));
            alertContext.add(Pair.of("关联订单数", String.valueOf(cnt)))
            return true;
        } else {
            int skuInt = handleRiskLogicPropertiesSumByCounter(orderItemSku, key, "quantity", slidwidth);
            logger.info("highRisk sku:{},slidwidth:{},quantity:{}", orderItemSku, slidwidth, skuInt);
            if (skuInt > threshold) {
                alertContext.add(Pair.of("sku", orderItemSku))
                alertContext.add(Pair.of("sku总量", handleRiskLogicPropertiesSumByCounter(orderItemSku, key, "quantity", slidwidth)))
                int totalPay = handleRiskLogicPropertiesSumByCounter(orderItemSku, key, "pay", slidwidth);
                int totalPurchase = handleRiskLogicPropertiesSumByCounter(orderItemSku, key, "purchase", slidwidth);
                alertContext.add(Pair.of("累计收入(含鲜币)", totalPay))
                alertContext.add(Pair.of("累计成本", totalPurchase))
                alertContext.add(Pair.of("总支付利润率", handleMiddlePayRate(totalPay, totalPurchase) + "%"))
                handleAttachBindedOrderContext(key, slidwidth, alertContext);
                return true;
            }
        }
    }

    // 1天内，影响商品数量大于等于10，利润率 小雨等于-0.6
    private boolean handleHighRiskLevelLogic(final String sku, List<Pair<String, Object>> alertContext) {
        String key = makeHighRiskLevelKey(sku)
        boolean ret = handleRiskLogicByCounter(key, sku, 60 * 60 * 24, 10, alertContext);
        alertContext.add(Pair.of("统计周期", "最近1天"));
        return ret;
    }

    Map<String, Object> handle(JSONObject jsonObject) {
        Map<String, Object> retData = DecisionResultUtil.wrapNormal();
        String orderItemSku = null;
        String orderId = null;
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            // 售卖平台检查
            handleBussinessType(jsonObject)
            // 参数检查
            handleParamCheck(jsonObject);
            // 看用户是否是白名单用户
            handleIgnoreByNamedlist(jsonObject);
            // 基本参数提取
            orderItemSku = MapPathUtil.parse("orderItem.sku", jsonObject)
            orderId = MapPathUtil.parse("order.orderId", jsonObject)
            Long quantity = MapPathUtil.parseLong("orderItem.quantity", jsonObject)
            Long purchasePrice = MapPathUtil.parseLong("orderItem.purchasePrice", jsonObject)
            Long payPrice = MapPathUtil.parseLong("orderItem.payFee", jsonObject)
            // 60 为 鲜币支付 see com.mryx.ark.sdk.enums.OrderPriceFeeTypeEnum
            if (null != MapPathUtil.parseLong("orderFeeSplits.60.price", jsonObject)) {
                payPrice += MapPathUtil.parseLong("orderFeeSplits.60.price", jsonObject);
            }
            Long timestamp = MapPathUtil.parseLong("order.createdTime", jsonObject)
            // 优先积累商品数
            List<Pair<String, Object>> alertContext = new ArrayList<>();
            alertContext.add(Pair.of("sku名称", MapPathUtil.parse("orderItem.productName", jsonObject)))
            int payRate = handleMiddlePayRate(payPrice, purchasePrice);
            logger.info("商品售卖成本:rate:{},sku:{},order:{},paypreice{},purchase{},quantity{}", payRate, orderItemSku, orderId, payPrice, purchasePrice, quantity)
            if (payRate < -60) {
                countHighOrder(orderItemSku, orderId, purchasePrice, payPrice, quantity, timestamp)
                if (handleHighRiskLevelLogic(orderItemSku, alertContext) ) {
                    // 命中
                    retData = DecisionResultUtil.wrapAlert(alertContext);
                }
            } else {
                // 非检查区间
                throw new FailfastException(null, "非检查区间");
            }
        } catch (FailfastException | IllegalParamException skipException) {
            logger.info("云超:京东，跳过检查:{}", skipException.getMessage())
            retData = DecisionResultUtil.wrapNormal();
        } catch (Exception e) {
            logger.info("云超:京东，1分钟内，sku售卖数监测异常{}", jsonObject.toJSONString(), e);
            retData = DecisionResultUtil.wrapNormal();
        } finally {
            logger.info("sku售卖检查结果:{},{},result:{},total:{}", orderItemSku, orderId, JSON.toJSONString(jsonObject), stopwatch.elapsed(TimeUnit.MICROSECONDS));
        }
        return retData;
    }
}
