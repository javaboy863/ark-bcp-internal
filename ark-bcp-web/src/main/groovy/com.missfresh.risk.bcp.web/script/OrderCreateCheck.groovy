package script
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Maps
import com.missfresh.risk.bcp.domain.vo.HitVO
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

/**
 * @Author: Wangbo
 * @Description:
 * @Date: create in 2:47 PM 2021/5/14
 * @Modified by: 
 */
class OrderCreateCheck {
    private static final Logger logger = LoggerFactory.getLogger("OrderCreateCheck")
    private static final String ARK_APPCODE = "mryx-ark-top"
    private static final String ORDER_CENTER_APPCODE = "wuliu-order-center-provider"
    Map handle(JSONObject jsonObject) {
        Boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        try{
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>> )jsonObject
            List<HitVO> arkHits = jsonMap.get(ARK_APPCODE)
            List<HitVO> orderCenterHits = jsonMap.get(ORDER_CENTER_APPCODE)
            Map<String,String> arkOrderMap = getCurrentSystemOrderMap(arkHits,"推送履约平台,req:","orderNo")
            Map<String,String> orderCenterOrderMap = Maps.newHashMap()
            if(!CollectionUtils.isEmpty(orderCenterHits)){
                orderCenterOrderMap = getCurrentSystemOrderMap(orderCenterHits,"接收交易订单 dto = ","orderNo")
            }
            for(Map.Entry<String,String> entry:arkOrderMap.entrySet()){
                if(!orderCenterOrderMap.containsKey(entry.key)){
                    checkSign = true
                    sb.append("履约系统未接收到交易订单号:"+entry.key+"\n")
                }
            }
        }catch(Exception e){
            System.out.println("Exception")
            logger.info("交易链路订单检查异常", e)
        }
        logger.info("交易链路订单检查完成")
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        return map
    }
    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Map<String,String> getCurrentSystemOrderMap(List<HitVO> hitVOList,String keyword,String keyField){
        Map<String,String> orderMap = Maps.newHashMap()
        for(HitVO orderCenterHitVO : hitVOList){
            String logDetail = orderCenterHitVO.getMessage()
            if(!StringUtils.isEmpty(logDetail)){
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),logDetail.length())
                JSONObject logObj = JSON.parseObject(logJson)
                String keyFeildVal = logObj.getString(keyField)
                orderMap.put(keyFeildVal,keyFeildVal)
            }
        }
        return orderMap
    }
}
