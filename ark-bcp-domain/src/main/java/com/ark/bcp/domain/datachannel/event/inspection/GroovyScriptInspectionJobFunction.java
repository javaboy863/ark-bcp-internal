

package com.ark.bcp.domain.datachannel.event.inspection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.datachannel.channel.EventMessageListenner;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.vo.LoadDataGroovyScriptVO;
import com.missfresh.risk.bcp.enums.DispatchStrategyDefine;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import io.elasticjob.lite.api.ShardingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 */
public class GroovyScriptInspectionJobFunction implements InspectionSimpleJobFunction {
    private final static Logger logger = LoggerFactory.getLogger(GroovyScriptInspectionJobFunction.class);
    private GroovyObject groovyObject = null;
    private Method loadDataMethod = null;

    private LoadDataGroovyScriptVO loadDataGroovyScriptVO;
    private EventMessageListenner eventMessageListenner;


    public GroovyScriptInspectionJobFunction(LoadDataGroovyScriptVO loadDataGroovyScriptVO, EventMessageListenner eventMessageListenner) {
        this.loadDataGroovyScriptVO = loadDataGroovyScriptVO;
        this.eventMessageListenner = eventMessageListenner;
        init();
    }

    private void init() {
        try {
            if (null == loadDataGroovyScriptVO) {
                return;
            }
            GroovyClassLoader classLoader = new GroovyClassLoader();
            Class<?> groovyScriptClass = classLoader.parseClass(loadDataGroovyScriptVO.getDynamicCodeSrc());
            if (null == groovyScriptClass) {
                return;
            }
            loadDataMethod = groovyScriptClass.getMethod("loadData", int.class, String.class, int.class);
            groovyObject = (GroovyObject) groovyScriptClass.newInstance();
            return;
        } catch (Exception e) {
            logger.error("加载脚本异常", e);
            return;
        }
    }

    /**
     * 执行作业.
     *
     * @param shardingContext 分片上下文
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        if (null == eventMessageListenner
                || null == loadDataGroovyScriptVO
                || null == groovyObject
                || null == loadDataMethod) {
            logger.info("something is null");
            return;
        }
        try {
            Object ret = loadDataMethod.invoke(
                    groovyObject,
                    shardingContext.getShardingItem(),
                    shardingContext.getShardingParameter(),
                    shardingContext.getShardingTotalCount());
            if (ret instanceof List) {
                DispatchStrategyDefine dispatchStrategyDefine = DispatchStrategyDefine.fromStrategy(loadDataGroovyScriptVO.getDispatchStrategy());
                if (DispatchStrategyDefine.PACKAGE_DISPATCH == dispatchStrategyDefine) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("data", ret);
                    EventMessageEntity<?> eventMessageEntity = EventMessageEntity.builder()
                            .messageId(UUID.randomUUID().toString())
                            .messageBody(jsonObject)
                            .rawBody(JSON.toJSONString(jsonObject))
                            .receiveTime(new Date())
                            .build();
                    eventMessageListenner.onMesssage(eventMessageEntity);
                } else if (DispatchStrategyDefine.BYLINE_DISPATCH == dispatchStrategyDefine) {
                    for (Object o : ((List<?>) ret)) {
                        EventMessageEntity<?> eventMessageEntity = EventMessageEntity.builder()
                                .messageId(UUID.randomUUID().toString())
                                .messageBody(JSON.parseObject(JSON.toJSONString(o)))
                                .rawBody(JSON.toJSONString(o))
                                .receiveTime(new Date())
                                .build();
                        eventMessageListenner.onMesssage(eventMessageEntity);
                    }
                } else {
                    // 不投递
                    logger.info("事件不投递");
                }
            }
        } catch (Exception e) {
            logger.info("调用失败:", e);
        }
    }

    @Override
    public void close() {

    }
}
