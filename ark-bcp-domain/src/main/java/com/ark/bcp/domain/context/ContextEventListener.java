package com.ark.bcp.domain.context;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.constant.EventTriggerTypeEnum;
import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.EventMessageHandler;
import com.ark.bcp.domain.datachannel.EventSourceFactory;
import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.repository.riskbcp.EventSourceConfigRepository;
import com.ark.bcp.domain.service.DecisionService;
import com.ark.bcp.domain.service.EventSourceConfigDomainService;
import com.ark.bcp.domain.service.InspectionEventSourceConfigDomainService;
import com.missfresh.risk.bcp.domain.datachannel.*;
import com.ark.bcp.domain.engine.frame.ComponentTypeEnum;
import com.ark.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.ark.bcp.domain.engine.frame.rss.NotifyListener;
import com.ark.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 */
@Component
public class ContextEventListener implements InitializingBean, EventMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ContextEventListener.class);
    @Resource
    private EventSourceConfigRepository eventSourceConfigRepository;
    @Resource
    private EventSourceFactory eventSourceFactory;
    @Resource
    private DecisionService decisionService;

    @Resource
    private EventSourceConfigDomainService eventSourceConfigDomainService;

    @Resource
    private InspectionEventSourceConfigDomainService iescdService;

    @EventListener({ContextRefreshedEvent.class})
    public void handleContextRefreshed() {
        // 应用启动时创建EventSource
        List<Long> eventSourceIds = eventSourceConfigRepository.queryIdsByCondition(
                EventSourceConfigEntity.builder()
                        .isDelete(0)
                        .build());
        if (CollectionUtils.isEmpty(eventSourceIds)) {
            return;
        }
        for (Long sourceId : eventSourceIds) {
            try {
                BaseEventSource eventSource = eventSourceFactory.getById(sourceId);
                if (null != eventSource) {
                    eventSource.setEventListner(this);
                    eventSource.initDataChannel();
                }
            } catch (Exception e) {
                logger.error("加载事件异常:", e);
            }
            logger.info("启动");
        }

        // 应用启动时创建CheckRule

        // 将CheckRule注册到对应的EventSource上
    }

    @EventListener({ContextStoppedEvent.class})
    public void handleContextStopped() {
        // 关闭EventSource
        for (Long sourceId : eventSourceFactory.getAllEventSourceIds()) {
            eventSourceFactory.closeById(sourceId);
        }
    }


    private void registerEventConfigChangeEvent() {
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        configChangeNotifier.subscribe(Constant.BROADCAST_ID_EVENT, new NotifyListener() {
            @Override
            public void onNotify(String topic, String serializable) {
                if (!Constant.BROADCAST_ID_EVENT.equalsIgnoreCase(topic)) {
                    return;
                }
                try {
                    PublishParams params = JSON.parseObject(serializable, PublishParams.class);
                    if (null != params && ComponentTypeEnum.EVENT == params.getComponentTypeEnum()) {
                        upsertEventSource(Long.valueOf(params.getTypeId()));
                    }
                } catch (Exception e) {
                    // donothing
                    logger.info("配置变更转换失败:", e);
                }
            }
        });
    }

    private void upsertEventSource(Long eventid) {
        BaseEventSource eventSource = eventSourceFactory.getById(eventid);
        if (null == eventSource) {
            return;
        }
        if (!eventSourceFactory.contains(eventid)) {
            eventSource.setEventListner(this);
            eventSource.initDataChannel();
            logger.info("broadcast of event:{},已经新建", eventid);
        } else {
            EventSourceConfigEntity entity = eventSourceConfigRepository.selectById(eventid);
            EventTriggerTypeEnum triggerTypeEnum = EventTriggerTypeEnum.fromType(entity.getTriggerType());
            if (EventTriggerTypeEnum.INSPECTION == triggerTypeEnum) {
                try {
                    entity = iescdService.getById(eventid);
                } catch (Exception e) {
                    logger.error("获取巡检事件配置异常", e);
                }
            }
            if (null != entity) {
                eventSource.setEventSourceConfigEntity(entity);
                eventSource.initDataChannel();
                logger.info("broadcast of event:{},已更新", eventid);
            }
        }
        logger.info("broadcast of event:{}", eventid);
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        registerEventConfigChangeEvent();
    }


    /**
     * 收到消息通知
     *
     * @param eventMessage
     * @return
     */
    @Override
    public boolean handle(EventMessageEntity<?> eventMessage) {
        logger.info("执行方法ContextEventListener.handle");
        if (null == eventMessage || null == eventMessage.getDataSourceId() || 0 == eventMessage.getDataSourceId()) {
            logger.info("消息丢弃:{}", eventMessage);
            return true;
        }
        decisionService.decision(eventMessage.getDataSourceId(), eventMessage);
        return true;
    }
}
