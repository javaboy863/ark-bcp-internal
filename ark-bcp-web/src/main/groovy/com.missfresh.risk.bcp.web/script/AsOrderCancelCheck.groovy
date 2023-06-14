package com.missfresh.risk.bcp.web.script
import com.ark.bcp.web.web.script.DynamicDubboInvoke
import com.alibaba.fastjson.JSONObject
import com.ark.bcp.web.web.script.DynamicDubboInvoke
import com.missfresh.risk.bcp.domain.util.MapPathUtil
import org.springframework.util.CollectionUtils
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory

class AsOrderCancelCheck {
    private static final Logger logger = LoggerFactory.getLogger(AsOrderCancelCheck.class);
    Boolean handle(JSONObject jsonObject) {
        String orderStatus = jsonObject.getString("")
        if ("ORDER_CREATED" != orderStatus) {
            logger.info("AS检查订单是否取消结果:不是关心的状态{}", orderStatus);
            return false
        }

        String interfaceName = "com.mryx.ark.tos.api.OrderSearchService"
        String methodName = "simpleListOrders"
        String orderId = jsonObject.getString("orderId")
        String[] types = new String[2]
        types[0] = "java.lang.String"
        types[1] = "com.mryx.ark.tos.request.SimpleOrderReq"
        Map<String, Object> map = new HashMap<>()
        Set<String> orderIds = new HashSet<>()
        orderIds.add(orderId)
        Set<Integer> orderStatuses = new HashSet<>()
        orderStatuses.add(10)

        map.put("orderId", orderIds)
        map.put("orderStatus", orderStatuses)
        map.put("pageNum", 1)
        map.put("pageSize", 1)
        Object[] values = new Object[2]
        values[0] = "risk-bcp"
        values[1] = map
        logger.info("AS检查订单是否取消开始");
        Map<String,Object> obj = (Map<String, Object>) DynamicDubboInvoke.invokeDubboSimplified(interfaceName, methodName, types, values)
        logger.info("AS检查订单是否取消结果:{}", JSON.toJSONString(obj));
        Object retObj = MapPathUtil.parseObject("data", obj)
        logger.info("AS检查订单是否取消结果处理:{}", JSON.toJSONString(retObj));
        if (CollectionUtils.isEmpty(retObj)) {
            return false
        }
        return true
    }
}