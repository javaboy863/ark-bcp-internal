package script

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Lists
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.vo.HitVO
import com.mryx.monitor.api.BusinessMonitor
import org.springframework.util.CollectionUtils

/**
 * @Author: Wangbo
 * @Description:【大仓】调用ODS失败监控
 * - APP Code：wms-internal
 * @Date: create in 2:47 PM 2021/07/18
 * @Modified by:
 */
class WmsODSCheck {
    private static final Logger logger = LoggerFactory.getLogger("WmsODSCheck")
    private static final String WMS01_APPCODE = "wms-internal-1"
    private static final String WMS02_APPCODE = "wms-internal-2"
    private static final String WMS03_APPCODE = "wms-internal-3"
    private static final String WMS04_APPCODE = "wms-internal-4"
    private static final String WMS05_APPCODE = "wms-internal-5"
    private static final String WMS06_APPCODE = "wms-internal-6"
    private static final String WMS07_APPCODE = "wms-internal-7"
    Map handle(JSONObject jsonObject) {
        Boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        List<String> keyList = Lists.newArrayList()
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        try{
            logger.debug("WmsODSCheck jsonObject:{}",jsonObject)
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>>)jsonObject
            Set<Map.Entry<String,List<HitVO>>> entrySet = jsonMap.entrySet()
            for(Map.Entry<String,List<HitVO>> entry: entrySet){
                if(!CollectionUtils.isEmpty(entry.value)){
                    keyList.add(entry.key)
                }
            }
            BusinessMonitor.recordOne("BCP_EVENT_28")
            if(CollectionUtils.isEmpty(keyList)){
                return map
            }
            checkSign = true
            sb.append("详情:").append(keyList.join(",").toString())
            BusinessMonitor.recordOne("BCP_EVENT_28_hited")
        }catch(Exception e){
            logger.info("【大仓】调用ODS失败监控规则异常", e)
        }
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        logger.info("【大仓】调用ODS失败监控规则完成,map:{}",JSON.toJSONString(map))
        return map
    }
}