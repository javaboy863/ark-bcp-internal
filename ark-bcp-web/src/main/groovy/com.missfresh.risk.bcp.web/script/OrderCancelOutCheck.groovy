package script

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Maps
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.vo.HitVO
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import com.mryx.monitor.api.BusinessMonitor
import java.text.SimpleDateFormat

/**
 * @Author: Wangbo
 * @Description:
 * @Date: create in 2:47 PM 2021/5/28
 * @Modified by:
 */
class OrderCancelOutCheck {
    private static final Logger logger = LoggerFactory.getLogger("OrderCancelOutCheck")
    private static final String ARK_APPCODE = "mryx-ark-tor"
    private static final String SMS_APPCODE = "sms-outbound"
    Map handle(JSONObject jsonObject) {
        boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        try{
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>> )jsonObject
            logger.info("OrderCancelOutCheck jsonMap:{}",JSON.toJSONString(jsonMap))
            List<HitVO> arkHits = jsonMap.get(ARK_APPCODE)
            if(CollectionUtils.isEmpty(arkHits)){
                return map
            }
            List<HitVO> smsHits = jsonMap.get(SMS_APPCODE)
            logger.info("OrderCancelOutCheck arkHits:{}",JSON.toJSONString(arkHits))
            logger.info("OrderCancelOutCheck smsHits:{}",JSON.toJSONString(smsHits))
            Map<String,Date> arkOrderMap = getCurrentSystemOrderMap(arkHits,"取消履约平台订单,req:","orderNo",",res:")
            logger.info("OrderCancelOutCheck arkOrderMap:{}",JSON.toJSONString(arkOrderMap))
            Map<String,Date> smsMap = Maps.newConcurrentMap()
            if(!CollectionUtils.isEmpty(smsHits)){
                smsMap = getSMSMap(smsHits,"监听发货单出库事件，data = ","relatedBusinessNo")
            }
            logger.info("OrderCancelOutCheck orderCenterOrderMap:{}",JSON.toJSONString(smsMap))
            for(Map.Entry<String,Date> entry:arkOrderMap.entrySet()){
                BusinessMonitor.recordOne("BCP_EVENT_25")
                if(smsMap.containsKey(entry.key)){
                    if(entry.value.compareTo(smsMap.get(entry.key)) == -1){
                        checkSign = true
                        sb.append("订单号:"+entry.key+";取消时间:"+entry.value.format("yyyy-MM-dd HH:mm:ss.SSS")+";出库时间:"+smsMap.get(entry.key).format("yyyy-MM-dd HH:mm:ss.SSS")+";\n")
                        BusinessMonitor.recordOne("BCP_EVENT_25_hited")
                    }
                }
            }
        }catch(Exception e){
            logger.info("订单取消后出库检查异常", e)
        }
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        logger.info("订单取消后出库检查完成,map:{}",JSON.toJSONString(map))
        if(checkSign){
            BusinessMonitor.recordOne("业务检查_订单取消后出库检查命中数")
        }
        BusinessMonitor.recordOne("业务检查_订单取消后出库检查单据数")
        return map
    }
    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Map<String,Date> getCurrentSystemOrderMap(List<HitVO> hitVOList,String keyword,String keyField,String lastkeyword){
        Map<String,String> orderMap = Maps.newConcurrentMap()
        for(HitVO orderCenterHitVO : hitVOList){
            logger.info("orderCenterHitVO:{}",JSON.toJSONString(orderCenterHitVO))
            String logDetail = orderCenterHitVO.getMessage()
            String dateStr = orderCenterHitVO.getTime()
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            Date date = ft.parse(dateStr)
            logger.info("OrderCancelOutCheck date:{}",date.format("yyyy-MM-dd HH:mm:ss.SSS"))
            logger.info("OrderCancelOutCheck logDetail:{}",logDetail)
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)){
                if(!Objects.isNull(lastkeyword)){
                    lastIndex = logDetail.indexOf(lastkeyword)
                }
                if(!logDetail.contains(keyword)){
                    continue
                }
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.info("OrderCancelOutCheck logJson:{}",logJson)
                JSONObject logObj = JSON.parseObject(logJson)
                logger.info("OrderCancelOutCheck logObj:{}",JSON.toJSONString(logObj))
                String keyFeildVal = logObj.getString(keyField)
                logger.info("OrderCancelOutCheck keyFeildVal:{}",keyFeildVal)
                orderMap.put(keyFeildVal,date)
            }
        }
        return orderMap
    }
    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Map<String,Date> getSMSMap(List<HitVO> hitVOList,String keyword,String keyField){
        Map<String,String> orderMap = Maps.newConcurrentMap()
        for(HitVO orderCenterHitVO : hitVOList){
            logger.info("getSMSMap smsHitVO:{}",JSON.toJSONString(orderCenterHitVO))
            String logDetail = orderCenterHitVO.getMessage()
            String dateStr = orderCenterHitVO.getTime()
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            Date date = ft.parse(dateStr)
            logger.info("getSMSMap date:{}",date.format("yyyy-MM-dd HH:mm:ss.SSS"))
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)){
                logger.info("getSMSMap logDetail:{},contains:{},keyword:{}",logDetail,logDetail.contains(keyword),keyword)
                if(!logDetail.contains(keyword)){
                    continue
                }
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.info("getSMSMap logJson:{}",logJson)
                JSONObject logObj = JSON.parseObject(logJson)
                logger.info("getSMSMap logObj:{}",JSON.toJSONString(logObj))
                String data = logObj.getString("data")
                logger.info("getSMSMap data:{}",data)
                String keyFeildVal = JSON.parseObject(data).getString(keyField)
                logger.info("getSMSMap keyFeildVal:{}",keyFeildVal)
                orderMap.put(keyFeildVal,date)
            }
        }
        return orderMap
    }
}
