

package com.ark.bcp.domain.datachannel.event.inspection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.vo.HitVO;
import com.ark.bcp.domain.vo.LoadDataLogInfoVO;
import com.ark.bcp.domain.vo.LogResultVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.datachannel.channel.EventMessageListenner;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.vo.*;
import com.missfresh.risk.bcp.dto.LogInfoDetailDto;
import com.mryx.arch.unifiedlog.facade.SearchLogService;
import com.mryx.arch.unifiedlog.vo.R;
import com.mryx.arch.unifiedlog.vo.RCode;
import com.mryx.arch.unifiedlog.vo.SearchLogRequest;
import io.elasticjob.lite.api.ShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import java.util.*;

/**
 */
@Slf4j
public class LogTypeInspectionJobFunction implements InspectionSimpleJobFunction {
    private final static Logger logger = LoggerFactory.getLogger(LogTypeInspectionJobFunction.class);

    private LoadDataLogInfoVO loadDataLogInfoVO;
    private EventMessageListenner eventMessageListenner;

    public LogTypeInspectionJobFunction() {
    }

    public LogTypeInspectionJobFunction(LoadDataLogInfoVO loadDataLogInfoVO, EventMessageListenner eventMessageListenner) {
        this.loadDataLogInfoVO = loadDataLogInfoVO;
        this.eventMessageListenner = eventMessageListenner;
//        init();
    }

    /**
     * 执行作业.
     *
     * @param shardingContext 分片上下文
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("LogTypeInspectionJobFunction execute jobName:{},loadDataLogInfoVO:{}",shardingContext.getJobName(),JSON.toJSONString(loadDataLogInfoVO));
        if (Objects.isNull(loadDataLogInfoVO)) {
            return;
        }
        if (CollectionUtils.isEmpty(loadDataLogInfoVO.getLogInfoDetailDtos())) {
            return;
        }
        Map<String, Object> results = getLogs();
        log.info("LogTypeInspectionJobFunction jobName:{},results:{}",shardingContext.getJobName(),JSON.toJSONString(results));
        if(!CollectionUtils.isEmpty(results)){
            log.info("日志校验发送数据");
            EventMessageEntity<?> eventMessageEntity = EventMessageEntity.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageBody(JSON.parseObject(JSON.toJSONString(results)))
                    .rawBody(JSON.toJSONString(results))
                    .receiveTime(new Date())
                    .build();
            eventMessageListenner.onMesssage(eventMessageEntity);
        }

    }

    private void init() {
        try {

        } catch (Exception e) {
            logger.error("加载数据源异常", e);
        }
    }

    @Override
    public void close() {

    }

    /**
     * 计算日志查询起止时间
     * @param loadDataLogInfoVO
     * @return
     */
    private Pair<Long,Long> calculateTime(LoadDataLogInfoVO loadDataLogInfoVO){
        Integer beforeMinute = loadDataLogInfoVO.getBeforeMinute();
        Integer timeRegion = loadDataLogInfoVO.getTimeRegion();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE,-beforeMinute);
        Long toTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE,-timeRegion);
        Long fromTime = calendar.getTimeInMillis();
        return Pair.of(fromTime,toTime);
    }

    /**
     * 拉取日志平台信息
     * @return
     */
    private Map<String, Object> getLogs(){
        Pair<Long,Long> timePair = calculateTime(loadDataLogInfoVO);
        Map<String, Object> results = Maps.newConcurrentMap();
        Long startTimequantum = 0L;
        Long endTimequantum = 0L;
        Iterator<LogInfoDetailDto> it = loadDataLogInfoVO.getLogInfoDetailDtos().iterator();
        int seq = 1;
        while (it.hasNext()) {
            LogInfoDetailDto logInfoDetailDto = it.next();
            List<HitVO> hits = getSignleSystemLog(logInfoDetailDto,timePair.getLeft()+startTimequantum,timePair.getRight()+endTimequantum);
            results.put(logInfoDetailDto.getAppCode()+"-"+String.valueOf(seq),hits);
            startTimequantum-=3L*1000L;
            endTimequantum+=5*60L*1000L;
            ++seq;
        }
        return results;
    }

    /**
     * 拉取单系统日志平台信息
     * @return
     */
    private List<HitVO> getSignleSystemLog(LogInfoDetailDto logInfoDetailDto,Long leftTime , Long rightTime){
        List<HitVO> hits = Lists.newArrayList();
        boolean sign = true;
        String scrollId = null;
        String keyword = convertKeyword(logInfoDetailDto.getLogKeyword());
        try {
            while (sign) {
                SearchLogRequest searchLogRequest = new SearchLogRequest();
                searchLogRequest.setAppCode(logInfoDetailDto.getAppCode());
                searchLogRequest.setQ(keyword);
                searchLogRequest.setFrom(leftTime);
                searchLogRequest.setTo(rightTime);
                searchLogRequest.setEnv("default");
                searchLogRequest.setScrollId(scrollId);
                SearchLogService searchLogService = AbstractApplicationContextUtil.getExtension(SearchLogService.class, "searchLogService");
                R result = searchLogService.search(searchLogRequest);
                log.info("searchLogService searchLogRequest:{},result:{}",JSON.toJSONString(searchLogRequest),JSONObject.toJSONString(result));
                if (RCode.SUCCESS.getCode().equals(result.getCode())) {
                    LogResultVO logResultVO = JSONObject.parseObject(JSON.toJSONString(result.getData()),LogResultVO.class);
                    log.debug("searchLogService logResultVO:{}",JSON.toJSONString(logResultVO));
                    if(!CollectionUtils.isEmpty(logResultVO.getHits())){
                        scrollId = logResultVO.getScrollId();
                        logResultVO.getHits().forEach(v->{
                            String message = v.getMessage().replaceAll("<span class=\"esTag\">","").replaceAll("</span>","");
                            v.setMessage(message);
                            log.info("searchLogService message:{}",message);
                            hits.add(v);
                        });
                    }else{
                        sign = false;
                    }
                }else{
                    log.error("定时查询日志平台失败,请求信息:{},返回信息:{}",JSON.toJSONString(searchLogRequest),JSON.toJSONString(result));
                }
            }
        } catch (Exception e) {
            log.error("定时查询日志平台异常,exception:{}",e);
        }
        return hits;
    }

    /**
     * 转换日志查询关键字
     * @param originKeyword
     * @return
     */
    private String convertKeyword(String originKeyword){
        return originKeyword.replaceAll(":"," ").replaceAll("="," ").replaceAll("："," ").replaceAll(","," ").replaceAll("，"," ").replaceAll("。"," ");
//        return originKeyword;
    }
}
