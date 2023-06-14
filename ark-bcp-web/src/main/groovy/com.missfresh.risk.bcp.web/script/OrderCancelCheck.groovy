package script

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.vo.HitVO
import com.mryx.monitor.api.BusinessMonitor
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

/**
 * @Author: Wangbo
 * @Description:
 * @Date: create in 2:47 PM 2021/5/28
 * @Modified by: 
 */
class OrderCancelCheck {
    private static final Logger logger = LoggerFactory.getLogger("OrderCancelCheck")
    private static final String ARK_APPCODE = "mryx-ark-tor"
    private static final String ORDER_CENTER_APPCODE = "wuliu-order-center-provider"
    Map handle(JSONObject jsonObject) {
        Boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        try{
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>> )jsonObject
            logger.info("OrderCancelCheck jsonMap:{}",JSON.toJSONString(jsonMap))
            List<HitVO> arkHits = jsonMap.get(ARK_APPCODE)
            if(CollectionUtils.isEmpty(arkHits)){
                return map
            }
            List<HitVO> orderCenterHits = jsonMap.get(ORDER_CENTER_APPCODE)
            logger.info("OrderCancelCheck arkHits:{}",JSON.toJSONString(arkHits))
            logger.info("OrderCancelCheck orderCenterHits:{}",JSON.toJSONString(orderCenterHits))
            println "orderCenterHits:"+JSON.toJSONString(orderCenterHits)
            Map<String,String> arkOrderMap = getArkOrderMap(arkHits,"取消履约平台订单,req:","orderNo","orderType",",res:")
            logger.info("OrderCancelCheck arkOrderMap:{}",JSON.toJSONString(arkOrderMap))
            Map<String,String> orderCenterOrderMap = Maps.newConcurrentMap()
            if(!CollectionUtils.isEmpty(orderCenterHits)){
                orderCenterOrderMap = getCurrentSystemOrderMap(orderCenterHits,"交易订单取消 dto = ","orderNo",null)
            }
            logger.info("OrderCancelCheck orderCenterOrderMap:{}",JSON.toJSONString(orderCenterOrderMap))
            for(Map.Entry<String,String> entry:arkOrderMap.entrySet()){
                BusinessMonitor.recordOne("BCP_EVENT_23")
                if(!orderCenterOrderMap.containsKey(entry.key)){
                    checkSign = true
                    sb.append("订单取消但履约未收到订单号:"+entry.key+"\n")
                    BusinessMonitor.recordOne("BCP_EVENT_23_hited")
                }
            }
            if(checkSign){
                logger.info("OrderCancelCheck jsonObject:{}",jsonObject)
            }
        }catch(Exception e){
            logger.info("订单取消但履约未收到检查异常", e)
        }
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        logger.info("订单取消但履约未收到检查完成,map:{}",JSON.toJSONString(map))
        return map
    }
    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Map<String,String> getCurrentSystemOrderMap(List<HitVO> hitVOList,String keyword,String keyField,String lastkeyword){
        Map<String,String> orderMap = Maps.newConcurrentMap()
        for(HitVO orderCenterHitVO : hitVOList){
            logger.info("orderCenterHitVO:{}",JSON.toJSONString(orderCenterHitVO))
            String logDetail = orderCenterHitVO.getMessage()
            logger.info("OrderCancelCheck logDetail:{}",JSON.toJSONString(logDetail))
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)){
                if(!Objects.isNull(lastkeyword)){
                    lastIndex = logDetail.indexOf(lastkeyword)
                }
                if(!logDetail.contains(keyword)){
                    continue
                }
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.info("OrderCancelCheck logJson:{}",JSON.toJSONString(logJson))
                JSONObject logObj = JSON.parseObject(logJson)
                logger.info("OrderCancelCheck logObj:{}",JSON.toJSONString(logObj))
                String keyFeildVal = logObj.getString(keyField)
                logger.info("OrderCancelCheck keyFeildVal:{}",JSON.toJSONString(keyFeildVal))
                orderMap.put(keyFeildVal,keyFeildVal)
            }
        }
        return orderMap
    }

    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keyword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Map<String,String> getArkOrderMap(List<HitVO> hitVOList,String keyword,String keyField,String keyField1,String lastkeyword){
        Map<String,String> orderMap = Maps.newConcurrentMap()
        for(HitVO orderCenterHitVO : hitVOList){
            logger.info("getArkOrderMap orderCenterHitVO:{}",JSON.toJSONString(orderCenterHitVO))
            String logDetail = orderCenterHitVO.getMessage()
            logger.info("getArkOrderMap logDetail:{}",JSON.toJSONString(logDetail))
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)){
                if(!Objects.isNull(lastkeyword)){
                    lastIndex = logDetail.indexOf(lastkeyword)
                }
                if(!logDetail.contains(keyword)){
                    continue
                }
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.info("getArkOrderMap logJson:{}",JSON.toJSONString(logJson))
                JSONObject logObj = JSON.parseObject(logJson)
                logger.info("getArkOrderMap logObj:{}",JSON.toJSONString(logObj))
                String orderNo = logObj.getString(keyField)
                int orderType = logObj.getIntValue(keyField1)
                logger.info("getArkOrderMap orderNo:{},orderType:{}",orderNo,orderType)
                if(10 == orderType){
                    orderMap.put(orderNo,orderNo)
                }
            }
        }
        return orderMap
    }
}
