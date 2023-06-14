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

/**
 * @Author: Wangbo
 * @Description:检查库存操作与流水不一致情况
 * - 单据&流水一致性场景定义：
 *   - 正常：单据入库一条，流水入库一条
 *   - 缺失：单据入库一条，流水入库没有
 *   - 重复：单据入库一条，流水入库大于一条
 * - 时效：
 *   - 5分钟
 *   - 可调成最大30分钟
 * - APP Code：logistics-wms-inventory
 * @Date: create in 2:47 PM 2021/06/29
 * @Modified by:
 */
class WmsInvCheck01 {
    private static final Logger logger = LoggerFactory.getLogger("WmsInvCheck01")
    private static final String INV01_APPCODE = "logistics-wms-inventory-1"
    private static final String INV02_APPCODE = "logistics-wms-inventory-2"
    Map handle(JSONObject jsonObject) {
        boolean checkSign = false
        StringBuilder sb = new StringBuilder("")
        Map<String,Object> map = new HashMap()
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        try{
            Map<String,List<HitVO>> jsonMap = (Map<String,List<HitVO>> )jsonObject
            List<HitVO> invHits01 = jsonMap.get(INV01_APPCODE)
            logger.debug("WmsInvCheck01 invHits01:{}",JSON.toJSONString(invHits01))
            List<HitVO> invHits02 = jsonMap.get(INV02_APPCODE)
            logger.debug("WmsInvCheck01 invHits02:{}",JSON.toJSONString(invHits02))
            if(CollectionUtils.isEmpty(invHits01)){
                return map
            }
//            invHits01 = getDistinctList(invHits01)
//            invHits02 = getDistinctList(invHits02)
            Set<String> invSet01 = getExecuteMap01(invHits01,"单据明细:")
            Set<String> invSet02 = getExecuteMap02(invHits02,"流水明细:")
            if(!CollectionUtils.isEmpty(invSet01)){
                for(String key:invSet01){
                    BusinessMonitor.recordOne("BCP_EVENT_26")
                    if(!invSet02.contains(key)){
                        checkSign = true
                        BusinessMonitor.recordOne("BCP_EVENT_26_hited")
                        sb.append("详情:"+key+"\n")
                    }
                }
            }
            if(checkSign){
                List<List<HitVO>> lists1 = Lists.partition(invHits01,500)
                for(List<HitVO> list : lists1){
                    logger.info("WmsInvCheck01 invHits:{}",JSON.toJSONString(list))
                }
                logger.info("WmsInvCheck01 invSet01:{}",JSON.toJSONString(invSet01))
                List<List<HitVO>> lists2 = Lists.partition(invHits01,500)
                for(List<HitVO> list : lists2){
                    logger.info("WmsInvCheck01 oooHits:{}",JSON.toJSONString(list))
                }
                logger.info("WmsInvCheck01 invSet02:{}",JSON.toJSONString(invSet02))
            }
        }catch(Exception e){
            logger.info("库存操作与流水检查异常", e)
        }
        map.put("hited",checkSign)
        map.put("msg",sb.toString())
        logger.info("库存操作与流水检查完成,map:{}",JSON.toJSONString(map))
        return map
    }

    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keyword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Set<String> getExecuteMap01(List<HitVO> hitVOList,String keyword){
        Set set = Sets.newConcurrentHashSet()
        String changeType = "1"
        for(HitVO hitVO : hitVOList){
            String logDetail = hitVO.getMessage()
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)&&logDetail.contains("单据数据一致性校验")){
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.debug("getExecuteMap01 logJson:{}",logJson)
                JSONArray jsonArray= JSONArray.parseArray(logJson)
                for(int i=0;i<jsonArray.size();i++){
                    String billCode = jsonArray.getJSONObject(i).getString("billCode")
                    String abilityType = jsonArray.getJSONObject(i).getString("abilityType")
                    String inventoryId = jsonArray.getJSONObject(i).getString("inventoryId")
                    set.add(billCode+":"+abilityType+":"+inventoryId+":"+changeType)
                }
            }
        }
        return set
    }

    /**
     * 转化传入系统单据信息到哈希表存储
     * @param hitVOList 日志信息列表
     * @param keyword 日志关键字
     * @param keyField json中关键字段
     * @return
     */
    Set<String> getExecuteMap02(List<HitVO> hitVOList,String keyword){
        Set set = Sets.newConcurrentHashSet()
        for(HitVO hitVO : hitVOList){
            String logDetail = hitVO.getMessage()
            int lastIndex = logDetail.length()
            if(!StringUtils.isEmpty(logDetail)&&logDetail.contains("流水数据一致性校验")){
                String logJson = logDetail.substring(logDetail.indexOf(keyword) + keyword.length(),lastIndex)
                logger.debug("getExecuteMap02 logJson:{}",logJson)
                JSONArray jsonArray= JSONArray.parseArray(logJson)
                for(int i=0;i<jsonArray.size();i++){
                    String sourceBillCode = jsonArray.getJSONObject(i).getString("sourceBillCode")
                    String abilityType = jsonArray.getJSONObject(i).getString("abilityType")
                    String changeType = jsonArray.getJSONObject(i).getString("changeType")
                    String inventoryId = jsonArray.getJSONObject(i).getString("inventoryId")
                    set.add(sourceBillCode+":"+abilityType+":"+inventoryId+":"+changeType)
                }
            }
        }
        return set
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