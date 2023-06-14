

package com.ark.bcp.domain.engine.frame.decision.flow.handles;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import com.ark.bcp.domain.entity.EventTaskItemEntity;
import com.ark.bcp.domain.service.EventTaskItemService;
import com.ark.bcp.domain.vo.DelayEventResourceConfigDO;
import com.google.common.base.Stopwatch;
import com.ark.bcp.domain.engine.frame.decision.flow.DecisionFlowInterceptor;
import com.ark.bcp.domain.util.EventSourceConfigEntityUtils;
import com.missfresh.risk.bcp.enums.DelayTypeDefine;
import com.mryx.common.utils.DateUtil;
import com.mryx.monitor.api.BusinessMonitor;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 */
@Component
@Order(30)
public class MessageDelayHandleInterceptor implements DecisionFlowInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MessageDelayHandleInterceptor.class);

    @Resource
    private EventTaskItemService eventTaskItemService;


    @Override
    public boolean handle(
            @NonNull final EventSourceConfigEntity eventSourceConfigEntity,
            @NonNull final EventMessageEntity<?> messageEntity) throws Exception {

        // 是否是延迟任务
        Date delayTime = delayTime(eventSourceConfigEntity);
        if (null == delayTime) {
            return false;
        }

        saveDelayMessage(messageEntity, delayTime);
        logger.info("event is delay:{},{}", eventSourceConfigEntity.getId(), messageEntity.getMessageId());
        MultiTagMonitor.record(MultiTagItem.build(
                EventSourceConfigEntityUtils.decisionMonitorId(eventSourceConfigEntity),
                EventSourceConfigEntityUtils.decisionMonitorName(eventSourceConfigEntity)).addTag("reason", "async_run"));
        return true;
    }

    private Date delayTime(EventSourceConfigEntity configEntity) {
        final DelayTypeDefine delayTypeDefine = DelayTypeDefine.fromCode(configEntity.getDelayTypeCode());
        DelayEventResourceConfigDO delayEventResourceConfigDO = JSON.parseObject(configEntity.getDelayTypeParam(), DelayEventResourceConfigDO.class);
        if (null == delayEventResourceConfigDO) {
            return null;
        }
        if (DelayTypeDefine.DELAY_X_MIN == delayTypeDefine) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, delayEventResourceConfigDO.getDelayxMinValue());
            return calendar.getTime();
        } else if (DelayTypeDefine.DELAY_AT == delayTypeDefine) {
            Date now = new Date();
            Date exeDate = DateUtil.parse(DateUtil.formatDate(now) + delayEventResourceConfigDO.getDelayAtValue());
            if (exeDate.after(now)) {
                return exeDate;
            }
            return null;
        }
        return null;
    }

    private void saveDelayMessage(EventMessageEntity<?> eventMessage, Date delayTime) {
        logger.info("save delay message:{} on {}", eventMessage.getMessageId(), eventMessage.getDataSourceId());
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            EventTaskItemEntity taskItem = new EventTaskItemEntity();
            Random random = new Random();
            int sharding = random.nextInt(EventTaskItemEntity.MAX_SHARDING);
            taskItem.setEventId(eventMessage.getDataSourceId());
            taskItem.setSharding(sharding);
            taskItem.setMessageId(eventMessage.getMessageId());
            taskItem.setRuleId(eventMessage.getDataSourceId());
            taskItem.setReceiveTime(eventMessage.getReceiveTime());
            taskItem.setEventMessage(eventMessage.getRawBody());
            taskItem.setStatus(0);
            taskItem.setExpireTime(delayTime);
            taskItem.setRetryTime(0);
            int effectRows = eventTaskItemService.add(taskItem);
            if (effectRows <= 0) {
                BusinessMonitor.recordOne("busi-message_save_task_fail", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        } catch (Exception e) {
            logger.info("save task error", e);
            BusinessMonitor.recordOne("busi-message_save_task_error", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }
}
