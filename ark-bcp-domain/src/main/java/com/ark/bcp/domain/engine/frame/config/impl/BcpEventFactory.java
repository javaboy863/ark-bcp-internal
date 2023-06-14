

package com.ark.bcp.domain.engine.frame.config.impl;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.decision.execute.event.AbstractExecutableBO;
import com.ark.bcp.domain.engine.frame.decision.execute.event.EventBo;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.engine.frame.ComponentTypeEnum;
import com.ark.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.ark.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import com.ark.bcp.domain.util.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;

/**
 */
public class BcpEventFactory {
    private static final Logger logger = LoggerFactory.getLogger(BcpEventFactory.class);
    private final Object invokeEntrySyncObject = new Object();

    private volatile Map<Long, AbstractExecutableBO> eventBoMap = Maps.newConcurrentMap();

    @Resource
    private BcpConfigSetService bcpConfigSetService;

    protected void initNotify() {
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");

        configChangeNotifier.subscribe(Constant.BROADCAST_ID_EVENT, (topic, serializable) -> {
            if (!Constant.BROADCAST_ID_EVENT.equalsIgnoreCase(topic)) {
                return;
            }
            try {
                PublishParams params = JSON.parseObject(serializable, PublishParams.class);
                if (null == params || ComponentTypeEnum.EVENT != params.getComponentTypeEnum()) {
                    return;
                }
                Long eventId = Long.valueOf(params.getTypeId());
                AbstractExecutableBO oldBo = eventBoMap.remove(eventId);
                if (null != oldBo) {
                    oldBo.releaseNotify();
                }
                logger.info("on command:{}", JSON.toJSONString(params));
                if (params.getAction() != PublishParams.ActionType.DEL) {
                    upsertEventBo(eventId);
                }
            } catch (Exception e) {
                // donothing
                logger.info("配置变更转换失败:", e);
            }
        });
    }

    public AbstractExecutableBO getExecutableBO(Long eventid) {
        AbstractExecutableBO executableBO = null;
        try {
            do {
                if (eventBoMap.containsKey(eventid)) {
                    executableBO = eventBoMap.get(eventid);
                    break;
                }
                executableBO = upsertEventBo(eventid);
            } while (false);
        } catch (Exception e) {
            logger.error("获取executableBO 异常:{}",eventid, e);
        } finally {
            logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "getInvokeEntryByEvent:{}->{}", eventid, executableBO);
        }
        return executableBO;
    }

    private AbstractExecutableBO upsertEventBo(Long eventid) {
        synchronized (invokeEntrySyncObject) {
            logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "getInvokeEntryByEvent lock:{}", eventid);
            AbstractExecutableBO executableBO = eventBoMap.get(eventid);
            if (null != executableBO) {
                logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "getInvokeEntryByEvent lock retry:{}", eventid);
                return executableBO;
            }
            try {
                EventSourceConfigEntity eventSourceConfigEntity = bcpConfigSetService.getEventConfigById(eventid);
                if (null != eventSourceConfigEntity) {
                    EventBo eventBo = new EventBo(eventSourceConfigEntity);
                    eventBoMap.put(eventid, eventBo);
                    logger.info(Namespace.ENGINE_CONFIG_LOG_PREFIX + "getInvokeEntryByEvent new eventbo:{}", eventid);
                } else {
                    logger.warn(Namespace.ENGINE_CONFIG_LOG_PREFIX + "getInvokeEntryByEvent null entity:{}", eventid);
                }
            } catch (Exception e) {
                logger.error(Namespace.ENGINE_CONFIG_LOG_PREFIX + "getInvokeEntryByEvent error", e);
            }
        }
        return eventBoMap.get(eventid);
    }
}
