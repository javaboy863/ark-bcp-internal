

package com.ark.bcp.domain.datachannel.factories;

import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.event.inspection.DynamicInspectionEventSrouce;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.entity.InspectionEventSourceEntity;
import com.ark.bcp.domain.service.InspectionEventSourceConfigDomainService;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 */
@Component("inspectionEventSourceFactory")
public class InspectionEventSourceFactory implements IEventSourceFactory, InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(InspectionEventSourceFactory.class);

    @Resource
    private ZookeeperRegistryCenter regCenter;

    @Resource
    private InspectionEventSourceConfigDomainService iescdService;

    @Override
    public BaseEventSource createEventSource(final EventSourceConfigEntity eventSourceConfigEntity) {
        if (null == eventSourceConfigEntity) {
            logger.info("事件信息为空");
            return null;
        }
        try {
            logger.info("创建脚本定时事件:{}", eventSourceConfigEntity.getId());
            InspectionEventSourceEntity entity = iescdService.getById(eventSourceConfigEntity.getId());
            DynamicInspectionEventSrouce eventSource = new DynamicInspectionEventSrouce(entity, regCenter);
            logger.info("创建脚本定时事件成功:{}", entity.getId());
            return eventSource;
        } catch (Exception e) {
            logger.error("创建事件异常", e);
        }
        return null;
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
    }
}
