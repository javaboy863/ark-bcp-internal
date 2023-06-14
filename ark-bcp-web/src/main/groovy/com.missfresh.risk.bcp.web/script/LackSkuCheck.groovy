package script

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Maps
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.vo.HitVO
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import com.mryx.monitor.api.BusinessMonitor

/**
 * @Author: Wangbo
 * @Description:
 * @Date: create in 2:47 PM 2021/5/28
 * @Modified by:
 */
class LackSkuCheck {
    private static final Logger logger = LoggerFactory.getLogger("LackSkuCheck")
    private static final String ARK_APPCODE = "mryx-ark-tor"
    private static final String STOCK_APPCODE = "wuliu-stock"
    Map handle(JSONObject jsonObject) {
        boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        try{
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>> )jsonObject
//            logger.info("LackSkuCheck jsonMap:{}",JSON.toJSONString(jsonMap))
            List<HitVO> arkHits = jsonMap.get(ARK_APPCODE)
            List<HitVO> stockHits = jsonMap.get(STOCK_APPCODE)
            if(CollectionUtils.isEmpty(arkHits)){
                return map
            }
            Map<String,String> arkOrderMap = getExecuteMap(arkHits,"req:","orderId",null)
            Map<String,String> stockMap = getExecuteMapByStock(stockHits,"tId=",", skus",", skus")
//            logger.info("LackSkuCheck arkOrderMap:{}",JSON.toJSONString(arkOrderMap))
            if(!CollectionUtils.isEmpty(arkOrderMap)){
                for(Map.Entry<String,String> entry:arkOrderMap.entrySet()){
                    BusinessMonitor.recordOne("BCP_EVENT_24")
                    if(!stockMap.containsKey(entry.key)){
                        checkSign = true
                        BusinessMonitor.recordOne("BCP_EVENT_24_hited")
                        sb.append("缺品订单号:"+entry.key+";详情:"+entry.value+"\n")
                    }
                }
            }
        }catch(Exception e){
            logger.info("缺品发货检查异常", e)
        }
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        logger.info("缺品发货检查完成,map:{}",JSON.toJSONString(map))
        return map
    }
    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Map<String,String> getExecuteMap(List<HitVO> hitVOList,String keyword,String keyField1,String lastkeyword){
        Map<String,String> orderMap = Maps.newConcurrentMap()
        for(HitVO hitVO : hitVOList){
//            logger.info("lackSkuCheckHitVO:{}",JSON.toJSONString(hitVO))
            String logDetail = hitVO.getMessage()
//            logger.info("LackSkuCheck logDetail:{}",JSON.toJSONString(logDetail))
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)){
                if(!Objects.isNull(lastkeyword)){
                    lastIndex = logDetail.indexOf(lastkeyword)
                }
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
//                logger.info("LackSkuCheck logJson:{}",JSON.toJSONString(logJson))
                JSONObject logObj = JSON.parseObject(logJson)
//                logger.info("LackSkuCheck logObj:{}",JSON.toJSONString(logObj))
                String orderNo = logObj.getString(keyField1)
                // 获取afterSalesApplyOrderItems
                String keyFieldVal2 = logObj.getString("afterSalesApplyOrderItems")
//                logger.info("LackSkuCheck keyFieldVal2:{}",JSON.toJSONString(keyFieldVal2))
                JSONArray jsonArray= JSONArray.parseArray(keyFieldVal2);
                StringBuilder sb = new StringBuilder();
                for(int i=0;i<jsonArray.size();i++){
                    String sku = jsonArray.getJSONObject(i).getString("sku");
                    String quantity = jsonArray.getJSONObject(i).getString("quantity");
                    sb.append("sku:"+sku).append(",quantity:"+quantity+";");
                }
//                logger.info("LackSkuCheck orderNo:{},msg:{}",orderNo,sb.toString())
                orderMap.put(orderNo,sb.toString())
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
    Map<String,String> getExecuteMapByStock(List<HitVO> hitVOList,String keyword,String keyField1,String lastkeyword){
        Map<String,String> orderMap = Maps.newConcurrentMap()
        for(HitVO hitVO : hitVOList){
            String logDetail = hitVO.getMessage()
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)){
                if(!Objects.isNull(lastkeyword)){
                    lastIndex = logDetail.indexOf(lastkeyword)
                }
                String tId = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                orderMap.put(tId,tId)
            }
        }
        return orderMap
    }
}