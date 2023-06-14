package com.ark.bcp.app;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.missfresh.risk.bcp.api.TaskItemService;
import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.DecisionExcuteResult;
import com.missfresh.risk.bcp.domain.entity.EventMessageEntity;
import com.missfresh.risk.bcp.domain.entity.EventTaskItemEntity;
import com.missfresh.risk.bcp.domain.service.DecisionService;
import com.missfresh.risk.bcp.domain.service.EventTaskItemService;
import com.mryx.monitor.api.MultiTagMonitor;
import com.mryx.monitor.item.MultiTagItem;
import com.mryx.sentinel.Entry;
import com.mryx.sentinel.SphU;
import com.mryx.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 */
@Slf4j
@Service
@Configuration
public class EventTaskItemBizImpl implements TaskItemService {
    /**
     * 分页查询的最大页大小
     */
    private final static int MAX_PAGE_SIZE = 100;
    /**
     * 阻塞列表队列长度大小
     */
    private final static int LINK_BLOCKING_QUEUE_SIZE = 1024;
    /**
     * 线程工程创建线程时的线程名称格式化字符串
     */
    private final static String THREAD_NAME_FORMAT = "sharding-job-thread-%d";
    /**
     * 事件上报到monitor的keys
     */
    private final static String MONITOR_KEY = "busi-task_result";
    /**
     * 友好提示
     */
    private final static String RESULT_TIP_MSG = "定时任务运行结果";
    @Resource
    private EventTaskItemService eventTaskItemService;

    @Resource
    private DecisionService decisionService;

    @Resource(name = "shardingJobExecutor")
    private ExecutorService shardingJobExecutor;

    @Bean(name = "shardingJobExecutor")
    public ExecutorService getShardingJobExecutor() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2, 60, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(LINK_BLOCKING_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_FORMAT).build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 分片执行任务.
     *
     * @param shardingIndex ""
     * @param shardingTotal ""
     */
    @Override
    public void executeShardingTasks(final int shardingIndex, final int shardingTotal) {
        choiceExecuteSharding(shardingIndex, shardingTotal);
    }

    /**
     * 选择分片并执行任务
     *
     * @param shardingIndex
     * @param shardingTotal
     */
    private void choiceExecuteSharding(int shardingIndex, int shardingTotal) {
        for (int i = 0; i < EventTaskItemEntity.MAX_SHARDING; i++) {
            if (shardingIndex != i % shardingTotal) {
                // 不在这个分片
                continue;
            }
            executeShardingTasks(i);
        }
    }

    /**
     * 执行单片任务.
     *
     * @param shardingIndex ""
     */
    private void executeShardingTasks(final int shardingIndex) {
        Long lastId = 0L;
        // 构建查询任务条件
        EventTaskItemEntity entity = buildEventTaskItemEntity(shardingIndex);
        List<Long> tasks = null;
        do {
            // 查询任务列表
            tasks = selectTasks(entity, lastId);
            log.info("获取任务数:{} sharding:{}", tasks.size(), shardingIndex);
            // 最后一个id,下次查询的开始ID
            lastId = CollectionUtils.isEmpty(tasks) ? -1L : tasks.get(tasks.size() - 1);
            // 执行任务并判断是否执行成功
        } while (executeTask(tasks));
    }


    /**
     * 构造任务查询条件
     *
     * @param shardingIndex
     * @return
     */
    private EventTaskItemEntity buildEventTaskItemEntity(int shardingIndex) {
        EventTaskItemEntity entity = EventTaskItemEntity.builder()
                .sharding(shardingIndex)
                .id(0L)
                .expireTime(new Date())
                .retryTime(EventTaskItemEntity.MAX_RETRY)
                .build();
        return entity;
    }

    /**
     * 查询任务
     *
     * @param entity
     * @param lastId
     * @return
     */
    private List<Long> selectTasks(EventTaskItemEntity entity, Long lastId) {
        entity.setId(lastId);
        return eventTaskItemService.selectShardingEntityByPageSelective(entity, MAX_PAGE_SIZE);
    }

    /**
     * 执行任务
     *
     * @param tasks 任务标识集合
     * @return
     */
    private boolean executeTask(List<Long> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return false;
        }
        tasks.forEach(aLong -> {
            shardingJobExecutor.execute(() -> {
                executeOneTask(aLong);
            });
        });
        return true;
    }


    /**
     * 执行单个任务.
     *
     * @param taskId ""
     */
    private void executeOneTask(Long taskId) {
        if (null == taskId) {
            return;
        }
        String key = "risk.bcp.job.delay.tasks";
        wrapSimpleInterface(key, buildTaskSupplier(taskId), buildFailSupplier(taskId));
    }

    /**
     * 构建任务执行器
     *
     * @param taskId
     * @return
     */
    private Supplier<Object> buildTaskSupplier(Long taskId) {
        return () -> {
            EventTaskItemEntity entity = null;

            try {
                entity = eventTaskItemService.getById(taskId);
                if (null == entity || entity.isMaxRetry()) {
                    log.info("任务跳过:{}", JSON.toJSONString(entity));
                    return null;
                }
                // 创建任务事件
                EventMessageEntity eventMessage = buildEventMessageEntity(entity);
                if (null == eventMessage.getDataSourceId()) {
                    log.info("无效任务:{}", JSON.toJSONString(entity));
                    recordTaskExeResultMonitor(MONITOR_KEY + entity.getEventId(), RESULT_TIP_MSG + entity.getEventId(), "invaliable");
                    return null;
                }

                DecisionExcuteResult decisionExcuteResult = decisionService.decisionImmediately(eventMessage.getDataSourceId(), eventMessage);
                if (DecisionExcuteResult.isException(decisionExcuteResult)) {
                    recordTaskExeResultMonitor(MONITOR_KEY + entity.getEventId(), RESULT_TIP_MSG + entity.getEventId(), "decision_error");
                    setTaskRetry(taskId);
                } else {
                    recordTaskExeResultMonitor(MONITOR_KEY + entity.getEventId(), RESULT_TIP_MSG + entity.getEventId(), "success");
                    eventTaskItemService.deleteById(taskId);
                }
            } catch (Exception e) {
                taskEventFailBack(taskId, entity, e);
            }
            return null;
        };
    }

    /**
     * 任务异常时的failBack执行
     *
     * @param taskId
     * @param entity
     * @param e
     */
    private void taskEventFailBack(Long taskId, EventTaskItemEntity entity, Exception e) {
        log.warn("异步执行任务异常", e);
        if (null != entity) {
            recordTaskExeResultMonitor(MONITOR_KEY + entity.getEventId(), RESULT_TIP_MSG + entity.getEventId(), "error");
        }
        try {
            setTaskRetry(taskId);
        } catch (Exception ee) {
            log.error(ee.getMessage(),ee);
            // donothing
        }
    }

    /**
     * 上报执行结果到monitor
     *
     * @param monitorKey
     * @param tip
     * @param error
     */
    private void recordTaskExeResultMonitor(String monitorKey, String tip, String error) {
        MultiTagMonitor.record(MultiTagItem.build(monitorKey, tip)
                .addTag("result", error));
    }

    /**
     * 创建事件消息实体
     *
     * @param entity
     * @return
     */
    private EventMessageEntity buildEventMessageEntity(EventTaskItemEntity entity) {
        EventMessageEntity eventMessage = EventMessageEntity.builder().build();
        eventMessage.setDataSourceId(entity.getEventId());
        eventMessage.setMessageId(entity.getMessageId());
        eventMessage.setReceiveTime(entity.getCreateTime());
        eventMessage.setRawBody(entity.getEventMessage());
        eventMessage.setMessageBody(JSON.parseObject(entity.getEventMessage()));
        return eventMessage;
    }

    /**
     * 任务执行失败执行器
     *
     * @param taskId
     * @return
     */
    private Supplier<Object> buildFailSupplier(Long taskId) {
        return () -> {
            recordTaskExeResultMonitor(MONITOR_KEY, RESULT_TIP_MSG, "failover");
            setTaskRetry(taskId);
            return null;
        };
    }

    private void setTaskRetry(Long taskid) {
        EventTaskItemEntity entity = eventTaskItemService.getById(taskid);
        if (null == entity) {
            log.info("任务跳过:{}", JSON.toJSONString(entity));
            return;
        }
        entity.setRetryTime(null != entity.getRetryTime() ? entity.getRetryTime() + 1 : 1);
        if (entity.isMaxRetry()) {
            log.info("重试次数超过阈值，删除任务{}", JSON.toJSONString(entity));
            eventTaskItemService.deleteById(taskid);
        } else {
            eventTaskItemService.updateByIdSelective(entity);
        }
    }

    private void wrapSimpleInterface(
            String name,
            Supplier supplier,
            Supplier defaultValue) {

        Entry entry = null;
        try {
            entry = SphU.entry(name);
            supplier.get();
            return;
        } catch (Exception e) {
            if (BlockException.isBlockException(e)) {
                try {
                    log.info("触发限流");
                    // 如遇到限流，利用实现sleep
                    Thread.sleep(1000);
                } catch (Exception e1) {
                    // donothing
                }
            } else {
                log.error("wrapSimpleInterface error.name:{}", name, e);
            }
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        defaultValue.get();
    }
}
