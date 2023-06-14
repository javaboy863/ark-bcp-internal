package com.ark.bcp.domain.datachannel;

import com.google.common.collect.Maps;
import com.ark.bcp.domain.constant.EventTriggerTypeEnum;
import com.ark.bcp.domain.datachannel.factories.IEventSourceFactory;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.service.EventSourceConfigDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 事件源工厂类，负责EventSouce的创建和管理
 */
@Service
public class EventSourceFactory implements InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(EventSourceFactory.class);

    private final static ConcurrentMap<EventTriggerTypeEnum, IEventSourceFactory> FACTORY_MAP = Maps.newConcurrentMap();

    @Resource(name = "inspectionEventSourceFactory")
    private IEventSourceFactory inspectionEventSourceFactory;

    @Resource(name = "listenEventSourceFactory")
    private IEventSourceFactory listenEventSourceFactory;


    @Resource
    private EventSourceConfigDomainService eventSourceConfigService;

    private static final Map<Long, BaseEventSource> EVENT_SOURCE_MAP = new ConcurrentHashMap<>();

    public boolean contains(Long sourceId) {
        return EVENT_SOURCE_MAP.containsKey(sourceId);
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
        FACTORY_MAP.putIfAbsent(EventTriggerTypeEnum.INSPECTION, inspectionEventSourceFactory);
        FACTORY_MAP.putIfAbsent(EventTriggerTypeEnum.LISTEN, listenEventSourceFactory);
    }

    public BaseEventSource getById(Long sourceId) {
        if (sourceId == null) {
            logger.warn("根据ID获取对应的EventSource实例，无效入参id={}", sourceId);
            return null;
        }

        BaseEventSource eventSource = EVENT_SOURCE_MAP.get(sourceId);
        if (eventSource != null) {
            return eventSource;
        }

        synchronized (this) {
            // 双重检查
            eventSource = EVENT_SOURCE_MAP.get(sourceId);
            if (eventSource != null) {
                return eventSource;
            }
            eventSource = createEventSource(sourceId);
            if (eventSource != null) {
                EVENT_SOURCE_MAP.put(sourceId, eventSource);
            }
        }
        return eventSource;
    }


    private BaseEventSource createEventSource(Long sourceId) {
        logger.info("创建事件源,{}", sourceId);
        EventSourceConfigEntity eventSourceConfig = null;
        if (null == sourceId) {
            logger.warn("创建新的EventSource实例，事件源配置不存在，sourceId:{}", sourceId);
            return null;
        }
        eventSourceConfig = eventSourceConfigService.getById(sourceId);
        if (eventSourceConfig == null) {
            logger.warn("创建新的EventSource实例，事件源配置不存在或未开启，sourceId:{}", sourceId);
            return null;
        }

        EventTriggerTypeEnum triggerTypeEnum = EventTriggerTypeEnum.fromType(eventSourceConfig.getTriggerType());
        if (null == triggerTypeEnum) {
            logger.info("创建新的EventSource实例，触发类型不识别，sourceId:{}， triggerid:{}", sourceId, eventSourceConfig.getTriggerType());
            return null;
        }
        IEventSourceFactory factory = FACTORY_MAP.getOrDefault(triggerTypeEnum, null);
        if (null == factory) {
            logger.info("创建新的EventSource实例，触发类型不识别，sourceId:{}， triggerid:{}", sourceId, eventSourceConfig.getTriggerType());
            return null;
        }
        return factory.createEventSource(eventSourceConfig);
    }

    public boolean closeById(Long sourceId) {
        BaseEventSource eventSource = EVENT_SOURCE_MAP.get(sourceId);
        return closeEventSource(eventSource);
    }


    private boolean closeEventSource(BaseEventSource eventSource) {
        if (!(eventSource instanceof DynamicDataSource)) {
            return true;
        }
        synchronized (this) {
            try {
                ((DynamicDataSource) eventSource).stop();
                EVENT_SOURCE_MAP.remove(eventSource.getEventSourceConfigEntity().getId());
                return true;
            } catch (Exception e) {
                logger.error("关闭事件源异常，eventSource:{}", eventSource, e);
                return false;
            }
        }
    }

    public Collection<BaseEventSource> getAllEventSources() {
        return EVENT_SOURCE_MAP.values();
    }

    public Set<Long> getAllEventSourceIds() {
        return EVENT_SOURCE_MAP.keySet();
    }


}
