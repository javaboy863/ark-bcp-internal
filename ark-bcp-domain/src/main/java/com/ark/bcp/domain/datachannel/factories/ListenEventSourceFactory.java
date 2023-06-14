
package com.ark.bcp.domain.datachannel.factories;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.factories.listen.RocketMqListenEventSourceFactory;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.constant.EventSourceTypeEnum;
import com.ark.bcp.domain.datachannel.factories.listen.KafkaListenEventSourceFactory;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentMap;

/**
 */
@Component(value = "listenEventSourceFactory")
public class ListenEventSourceFactory implements IEventSourceFactory {

    private final static ConcurrentMap<EventSourceTypeEnum, IEventSourceFactory> EVENT_SROUCE_FACTORY_MAP = Maps.newConcurrentMap();
    private final static Logger logger = LoggerFactory.getLogger(ListenEventSourceFactory.class);

    static {
        EVENT_SROUCE_FACTORY_MAP.putIfAbsent(EventSourceTypeEnum.KAFKA, new KafkaListenEventSourceFactory());
        EVENT_SROUCE_FACTORY_MAP.putIfAbsent(EventSourceTypeEnum.ROCKETMQ, new RocketMqListenEventSourceFactory());
    }

    @Override
    public BaseEventSource createEventSource(final EventSourceConfigEntity eventSourceConfigEntity) {
        try {
            if (null == eventSourceConfigEntity) {
                logger.info("事件不能为空");
                return null;
            }
            final EventSourceTypeEnum eventSourceTypeEnum = EventSourceTypeEnum.fromType(eventSourceConfigEntity.getType());
            if (null == eventSourceTypeEnum) {
                logger.info("未知数据源类型:{}", JSON.toJSONString(eventSourceConfigEntity.getType()));
                return null;
            }
            IEventSourceFactory factory = EVENT_SROUCE_FACTORY_MAP.getOrDefault(eventSourceTypeEnum, null);
            if (null == factory) {
                logger.info("未注册工厂:{}", eventSourceConfigEntity.getType());
                return null;
            }
            return factory.createEventSource(eventSourceConfigEntity);
        } catch (Exception e) {
            logger.error("创建事件异常:{}",JSON.toJSONString(eventSourceConfigEntity.getType()), e);
            return null;
        }
    }
}
