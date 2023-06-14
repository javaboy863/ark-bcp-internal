

package com.ark.bcp.domain.service;

import com.ark.bcp.domain.entity.DynamicCodeConfigEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.exception.IllegalParamException;
import com.ark.bcp.domain.repository.riskbcp.EventSourceConfigRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ark.bcp.domain.entity.InspectionEventSourceEntity;
import com.ark.bcp.domain.util.DynamicCodeConfigEntitys;
import com.missfresh.risk.bcp.enums.LoadDataStrategyDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
@Service
public class InspectionEventSourceConfigDomainService {
    private static final Logger logger = LoggerFactory.getLogger(InspectionEventSourceConfigDomainService.class);

    @Resource
    private DynamicCodeConfigService dynamicCodeConfigService;

    @Resource
    private EventSourceConfigRepository eventSourceConfigRepository;

    /**
     * 最多存100个，做多存1s.
     */
    private LoadingCache<Long, InspectionEventSourceEntity> eventConfigCache =
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(30, TimeUnit.SECONDS)
                    .removalListener(notification -> {
                        logger.info(
                                "remove key[" + notification.getKey() + "],"
                                        + "value[" + notification.getValue() + "],"
                                        + "remove reason[" + notification.getCause() + "]");
                    })
                    .build(new CacheLoader<Long, InspectionEventSourceEntity>() {
                        @Override
                        public InspectionEventSourceEntity load(Long key) throws Exception {
                            try {
                                logger.info("eventConfigCache load new instance {}", key);
                                InspectionEventSourceEntity entity = getById(key);
                                logger.info("eventConfigCache load new instance {},result:{}", key, entity);
                                if (null != entity) {
                                    return entity;
                                }
                            } catch (Exception e) {
                                // donothing
                                logger.error("获取数据源信息异常", e);
                            }
                            return new InspectionEventSourceEntity();
                        }
                    });

    public InspectionEventSourceEntity getByIdWithCache(Long id) {
        try {
            return eventConfigCache.get(id);
        } catch (Exception e) {
            logger.info("获取event异常", e);
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public int saveEventSource(final InspectionEventSourceEntity iescEntity) {
        logger.info("新增事件源配置，saveEventSource:{}", iescEntity);
        if (null == iescEntity) {
            throw new IllegalParamException("实体为空");
        }
        // 数据加载方式如果是脚本类型，优先保存或更新脚本
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(iescEntity.getLoadDataStrategy());
        // 生成detailconfig
        iescEntity.toDetailConfig();
        // 保存事件本体
        int ret = eventSourceConfigRepository.insertSelective(iescEntity);
        // 保存脚本
        if (LoadDataStrategyDefine.GROOVY_SCRIPT == loadDataStrategyDefine && null != iescEntity.getLoadDataGroovyScriptVO()) {
            upsertEventSourceAddDynamicCode(iescEntity);
        }
        return ret;
    }

    private Long upsertEventSourceAddDynamicCode(final InspectionEventSourceEntity iescEntity) {
        DynamicCodeConfigEntity dynamicCodeConfigEntity = DynamicCodeConfigEntitys.transFromInspectionEventEntity(iescEntity);
        if (null != dynamicCodeConfigEntity.getId() && dynamicCodeConfigEntity.getId() > 0) {
            // 更新脚本
            DynamicCodeConfigEntity codeConfigEntity = dynamicCodeConfigService.queryCodeListById(dynamicCodeConfigEntity.getId());
            if (null == codeConfigEntity) {
                throw new FailfastException(null, "找不到脚本配置");
            }
            dynamicCodeConfigEntity.setVersion(codeConfigEntity.getVersion());
            dynamicCodeConfigService.update(dynamicCodeConfigEntity);
        } else {
            // 新建脚本
            dynamicCodeConfigService.addCode(dynamicCodeConfigEntity);
        }
        return dynamicCodeConfigEntity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateEventSource(final InspectionEventSourceEntity iescEntity) {
        logger.info("更新事件源配置，updateEventSource:{}", iescEntity);
        if (null == iescEntity) {
            throw new IllegalParamException("实体为空");
        }
        // 数据加载方式如果是脚本类型，优先保存或更新脚本
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(iescEntity.getLoadDataStrategy());

        // 生成detailconfig
        iescEntity.toDetailConfig();
        // 保存事件本体
        int ret = eventSourceConfigRepository.updateByIdSelective(iescEntity);

        if (LoadDataStrategyDefine.GROOVY_SCRIPT == loadDataStrategyDefine && null != iescEntity.getLoadDataGroovyScriptVO()) {
            upsertEventSourceAddDynamicCode(iescEntity);
        }
        return ret;
    }


    public InspectionEventSourceEntity getById(Long id) throws InvocationTargetException, IllegalAccessException {
        // 读取本体信息
        EventSourceConfigEntity entity = eventSourceConfigRepository.selectById(id);
        if (null == entity) {
            throw new FailfastException(null, "找不到实体对象");
        }
        InspectionEventSourceEntity iescEntity = new InspectionEventSourceEntity();
        BeanUtils.copyProperties(entity, iescEntity);
        iescEntity.parseDetailConfig();
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(iescEntity.getLoadDataStrategy());
        if (LoadDataStrategyDefine.GROOVY_SCRIPT == loadDataStrategyDefine && null != iescEntity.getLoadDataGroovyScriptVO()) {
            DynamicCodeConfigEntity codeConfigEntity = dynamicCodeConfigService.queryCodeListByEventId(iescEntity.getId());
            if (null == codeConfigEntity) {
                throw new FailfastException(null, "找不到脚本配置");
            }
            iescEntity.getLoadDataGroovyScriptVO().setDynamicCodeSrc(codeConfigEntity.getScriptContent());
            iescEntity.getLoadDataGroovyScriptVO().setDynamicCodeConfigId(codeConfigEntity.getId());
        }
        return iescEntity;
    }

    public List<InspectionEventSourceEntity> queryByPage(final EventSourceConfigEntity condition) {
        logger.info("queryByPage:{}", condition);
        List<EventSourceConfigEntity> entities = eventSourceConfigRepository.selectListPage(condition);
        List<InspectionEventSourceEntity> inspectionEventSourceEntities = entities.stream().map(new Function<EventSourceConfigEntity, InspectionEventSourceEntity>() {
            @Override
            public InspectionEventSourceEntity apply(EventSourceConfigEntity entity) {
                InspectionEventSourceEntity retEntity = new InspectionEventSourceEntity();
                try {
                    BeanUtils.copyProperties(entity, retEntity);
                    retEntity.parseDetailConfig();
                    return retEntity;
                } catch (Exception e) {
                    logger.error("copyProperties异常", e);
                }
                return null;
            }
        }).collect(Collectors.toList());
        while (inspectionEventSourceEntities.remove(null)) {

        }
        return inspectionEventSourceEntities;
    }

    public Long pageTotal(EventSourceConfigEntity condition) {
        logger.info("pageTotal:{}", condition);
        return eventSourceConfigRepository.pageTotal(condition);
    }
}
