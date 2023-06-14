package com.ark.bcp.domain.service;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.repository.riskbcp.EventMatchTempleteRepository;
import com.ark.bcp.domain.repository.riskbcp.EventSourceConfigRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ark.bcp.domain.constant.EventSourceStatusEnum;
import com.ark.bcp.domain.engine.frame.ComponentTypeEnum;
import com.ark.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.ark.bcp.domain.engine.frame.rss.NotifyListener;
import com.ark.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import com.ark.bcp.domain.entity.EventMatchTemplateEntity;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 */
@Slf4j
@Service
public class EventSourceConfigDomainService implements InitializingBean, ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(EventSourceConfigDomainService.class);

    @Resource
    private EventSourceConfigRepository eventSourceConfigRepository;

    @Resource
    private EventMatchTempleteRepository eventMatchTempleteRepository;

    @Resource
    private RuleConfigDomainService ruleConfigDomainService;
    /**
     * 最多存100个，做多存1s.
     */
    private LoadingCache<String, EventSourceConfigEntity> eventConfigCache =
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(30, TimeUnit.SECONDS)
                    .removalListener(notification -> {
                        logger.info(
                                "remove key[" + notification.getKey() + "],"
                                        + "value[" + notification.getValue() + "],"
                                        + "remove reason[" + notification.getCause() + "]");
                    })
                    .build(new CacheLoader<String, EventSourceConfigEntity>() {
                        @Override
                        public EventSourceConfigEntity load(String key) throws Exception {
                            try {
                                logger.info("eventConfigCache load new instance {}", key);
                                EventSourceConfigEntity entity = getById(Long.parseLong(key));
                                logger.info("eventConfigCache load new instance {},result:{}", key, entity);
                                if (null != entity) {
                                    return entity;
                                }
                            } catch (Exception e) {
                                // donothing
                                logger.error("获取数据源信息异常", e);
                            }
                            return EventSourceConfigEntity.builder().build();
                        }
                    });


    public EventSourceConfigEntity getByIdWithCache(Long id) {
        try {
            return eventConfigCache.get(String.valueOf(id));
        } catch (Exception e) {
            log.info("获取event异常", e);
        }
        return null;
    }


    /**
     * 按ID查询
     *
     * @param id
     * @return
     */
    public EventSourceConfigEntity getById(Long id) {
        EventSourceConfigEntity entity = eventSourceConfigRepository.selectById(id);
        EventMatchTemplateEntity matchTemplateEntity = eventMatchTempleteRepository.selectByEventSourceId(id);
        entity.setMatchTemplateEntity(matchTemplateEntity);
        return entity;
    }

    /**
     * 按name查询
     *
     * @param sourceName
     * @return
     */
    public EventSourceConfigEntity getByName(String sourceName) {
        EventSourceConfigEntity queryCond = EventSourceConfigEntity.builder().name(sourceName).build();
        List<EventSourceConfigEntity> configEntities = eventSourceConfigRepository.queryByCondition(queryCond);
        EventSourceConfigEntity result = CollectionUtils.isNotEmpty(configEntities) ? configEntities.get(0) : null;
        if (null != result) {
            EventMatchTemplateEntity matchTemplateEntity = eventMatchTempleteRepository.selectByEventSourceId(result.getId());
            result.setMatchTemplateEntity(matchTemplateEntity);
        }
        return result;
    }

    /**
     * 新增事件源配置
     *
     * @param eventSourceConfig
     * @return
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "riskBcpTxManager")
    public int add(@NonNull EventSourceConfigEntity eventSourceConfig) {
        log.info("新增事件源配置，eventSourceConfig:{}", eventSourceConfig);
        int effectRow = eventSourceConfigRepository.insertSelective(eventSourceConfig);
        EventMatchTemplateEntity eventMatchTemplateEntity = eventSourceConfig.getMatchTemplateEntity();
        if (null != eventMatchTemplateEntity && 0 != eventMatchTemplateEntity.getSaveToMatchDbFlag()) {
            eventMatchTemplateEntity.setEventSourceId(eventSourceConfig.getId());
            int matEffectRow = eventMatchTempleteRepository.insert(eventMatchTemplateEntity);
            if (matEffectRow == 0) {
                throw new FailfastException(null, "新增匹配KEY失败");
            }
        }
        return effectRow;
    }

    /**
     * 按ID更新事件源配置
     *
     * @param updateConfig
     * @return
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "riskBcpTxManager")
    public int updateById(@NonNull EventSourceConfigEntity updateConfig) {
        log.info("更新事件源配置，updateConfig:{}", updateConfig);

        int effectRow = eventSourceConfigRepository.updateByIdSelective(updateConfig);
        EventMatchTemplateEntity eventMatchTemplateEntity = updateConfig.getMatchTemplateEntity();
        if (null != eventMatchTemplateEntity && 0 != eventMatchTemplateEntity.getSaveToMatchDbFlag()) {
            eventMatchTemplateEntity.setEventSourceId(updateConfig.getId());
            int matEffectRow = eventMatchTempleteRepository.updateByEventSourceSelective(eventMatchTemplateEntity);
            if (matEffectRow == 0) {
                EventMatchTemplateEntity entity = eventMatchTempleteRepository.selectByEventSourceId(eventMatchTemplateEntity.getEventSourceId());
                if (null == entity) {
                    matEffectRow = eventMatchTempleteRepository.insert(eventMatchTemplateEntity);
                }
            }
            if (matEffectRow == 0) {
                throw new FailfastException(null, "更新匹配KEY失败");
            }
        }
        return effectRow;
    }

    /**
     * 按ID更新事件源配置，只更新有值字段
     *
     * @param updateConfig
     * @return
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "riskBcpTxManager")
    public int updateByIdSelective(@NonNull EventSourceConfigEntity updateConfig) {
        log.info("更新事件源配置，updateConfig:{}", updateConfig);
        int effectRow = eventSourceConfigRepository.updateByIdSelective(updateConfig);
        EventMatchTemplateEntity eventMatchTemplateEntity = updateConfig.getMatchTemplateEntity();
        if (null != eventMatchTemplateEntity && 0 != eventMatchTemplateEntity.getSaveToMatchDbFlag()) {
            eventMatchTemplateEntity.setEventSourceId(updateConfig.getId());
            int matEffectRow = eventMatchTempleteRepository.updateByEventSourceSelective(eventMatchTemplateEntity);
            if (matEffectRow == 0) {
                EventMatchTemplateEntity entity = eventMatchTempleteRepository.selectByEventSourceId(eventMatchTemplateEntity.getEventSourceId());
                if (null == entity) {
                    matEffectRow = eventMatchTempleteRepository.insert(eventMatchTemplateEntity);
                }
            }
            if (matEffectRow == 0) {
                throw new FailfastException(null, "更新匹配KEY失败");
            }
        }
        return effectRow;
    }

    /**
     * 删除指定ID的事件源配置，逻辑删除
     *
     * @param configId
     * @return
     */
    public int deleteById(Integer configId) {
        log.info("删除事件源配置，configId:{}", configId);
        return eventSourceConfigRepository.deleteById(configId);
    }

    public List<EventSourceConfigEntity> queryByPage(EventSourceConfigEntity condition) {
        log.info("queryByPage:{}", condition);
        return eventSourceConfigRepository.selectListPage(condition);
    }

    public Long pageTotal(EventSourceConfigEntity condition) {
        log.info("pageTotal:{}", condition);
        return eventSourceConfigRepository.pageTotal(condition);
    }

    public List<EventSourceConfigEntity> queryByName(String name) {
        log.info("query by name:{}", name);
        return eventSourceConfigRepository.queryByName(name);
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
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry"
        );
        configChangeNotifier.subscribe(Constant.BROADCAST_ID_EVENT, new NotifyListener() {
            @Override
            public void onNotify(String topic, String serializable) {
                if (!Constant.BROADCAST_ID_EVENT.equalsIgnoreCase(topic)) {
                    return;
                }
                try {
                    PublishParams params = JSON.parseObject(serializable, PublishParams.class);
                    if (null != params
                            && ComponentTypeEnum.EVENT == params.getComponentTypeEnum()) {
                        eventConfigCache.refresh(params.getTypeId());

                        logger.info("broadcast of event:{}", params.getTypeId());
                    }
                } catch (Exception e) {
                    // donothing
                    logger.info("配置变更转换失败:", e);
                }
            }
        });
    }


    public Boolean eventCloseable(final EventSourceConfigEntity eventSourceConfigEntity) {
        CheckRuleConfigEntity ruleConfigEntity = CheckRuleConfigEntity.builder()
                .eventId(eventSourceConfigEntity.getId())
                .isDelete(0)
                .status(EventSourceStatusEnum.OPEN.getCode())
                .build();

        List<CheckRuleConfigEntity> checkRuleConfigEntities =
                ruleConfigDomainService.selectCheckRuleConfigList(ruleConfigEntity);
        return CollectionUtils.isEmpty(checkRuleConfigEntities);
    }

    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     * <p>Invoked after population of normal bean properties but before an init callback such
     * as {@link InitializingBean#afterPropertiesSet()}
     * or a custom init-method. Invoked after {@link ResourceLoaderAware#setResourceLoader},
     * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} and
     * {@link MessageSourceAware}, if applicable.
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws ApplicationContextException in case of context initialization errors
     * @throws BeansException              if thrown by application context methods
     * @see BeanInitializationException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AbstractApplicationContextUtil.addApplicationContext(applicationContext);
    }
}
