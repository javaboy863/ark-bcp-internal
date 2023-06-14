package script

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.missfresh.risk.bcp.domain.vo.HitVO
import com.mryx.monitor.api.BusinessMonitor
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.function.Predicate

/**
 * @Author: Wangbo
 * @Description:检查库存操作与流水重复消费规则
 * - APP Code：logistics-wms-inventory
 * @Date: create in 2:47 PM 2021/06/29
 * @Modified by:
 */
class WmsInvCheck02 {
    private static final Logger logger = LoggerFactory.getLogger("WmsInvCheck02")
    private static final String INV01_APPCODE = "logistics-wms-inventory-1"
    Map handle(JSONObject jsonObject) {
        Boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        Set<String> set = Sets.newConcurrentHashSet()
        try{
            logger.debug("WmsInvCheck02 jsonObject:{}",jsonObject)
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>>)jsonObject
            List<HitVO> invHits01 = jsonMap.get(INV01_APPCODE)
            if(CollectionUtils.isEmpty(invHits01)){
                return map
            }
            invHits01 = getDistinctList(invHits01)
            checkSign = getExecuteMap(invHits01,"流水明细:",set)
            if(checkSign){
                Iterator<String> it = set.iterator()
                while (it.hasNext()){
                    sb.append("详情:"+it.next()+"\n")
                }
            }
            if(checkSign){
                List<List<HitVO>> lists1 = Lists.partition(invHits01,300)
                for(List<HitVO> list : lists1){
                    logger.info("WmsInvCheck02 invHits01:{}",JSON.toJSONString(list))
                }
            }
        }catch(Exception e){
            logger.info("检查库存操作与流水重复消费规则异常", e)
        }
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        logger.info("检查库存操作与流水重复消费规则完成,map:{}",JSON.toJSONString(map))
        return map
    }

    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    boolean getExecuteMap(List<HitVO> hitVOList,String keyword,Set set){
        Boolean checkSign = false
        Set tempSet = Sets.newConcurrentHashSet()
        for(HitVO hitVO : hitVOList){
            String logDetail = hitVO.getMessage()
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)&&logDetail.contains("流水数据一致性校验")){
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.debug("getExecuteMap logJson:{}",logJson)
                JSONArray jsonArray= JSONArray.parseArray(logJson)
                for(int i=0;i<jsonArray.size();i++){
                    String sourceBillCode = jsonArray.getJSONObject(i).getString("sourceBillCode")
                    String abilityType = jsonArray.getJSONObject(i).getString("abilityType")
                    String billItemId = jsonArray.getJSONObject(i).getString("billItemId")
                    String changeType = jsonArray.getJSONObject(i).getString("changeType")
                    BusinessMonitor.recordOne("BCP_EVENT_27")
                    String key = sourceBillCode+":"+abilityType+":"+billItemId+":"+changeType
                    logger.debug("getExecuteMap key:{}",key)
                    if(!tempSet.add(key)){
                        checkSign = true
                        BusinessMonitor.recordOne("BCP_EVENT_27_hited")
                        set.add(key)
                    }
                }
            }
        }
        if(checkSign){
            logger.debug("WmsInvCheck02 tempSet:{}",JSON.toJSONString(tempSet))
        }
        return checkSign
    }

    /**
     * 因日志平台问题需要去重
     * @param hitVOList 日志信息列表
     * @return
     */
    List<HitVO> getDistinctList(List<HitVO> hitVOList){
        Set tempSet = Sets.newConcurrentHashSet()
        List list = Lists.newArrayList()
        for(HitVO hitVO : hitVOList){
            if(tempSet.add(hitVO.getTraceId()+":"+hitVO.getTime())){
                list.add(hitVO)
            }
        }
        return list
    }
}