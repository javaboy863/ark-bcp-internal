

package com.ark.bcp.web.web.job;


import com.google.common.base.Stopwatch;
import com.missfresh.risk.bcp.api.TaskItemService;
import com.mryx.monitor.api.BusinessMonitor;
import io.elasticjob.lite.api.ShardingContext;
import io.elasticjob.lite.api.simple.SimpleJob;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 每一分钟触发一次的任务.
 *
 */
@SuppressWarnings({"LineLength"})
public class EventTaskDirverJob implements SimpleJob {
    private final Logger logger = LoggerFactory.getLogger(EventTaskDirverJob.class);

    @Resource
    private TaskItemService taskItemService;

    @Override
    public void execute(ShardingContext shardingContext) {
        try {
            executeTask(shardingContext);
        } catch (Exception e) {
            taskExceptionHandler(e);
        }
    }


    /**
     * 定时任务执行异常处理方法
     *
     * @param e
     * @throws Exception
     */
    @SneakyThrows
    private void taskExceptionHandler(Exception e) {
        logger.error("EventTaskDirverJob exeute error", e);
        BusinessMonitor.recordOne("busi-dirverjob_error");
        throw e;
    }

    /**
     * 执行定时任务
     *
     * @param shardingContext
     */
    private void executeTask(ShardingContext shardingContext) {
        Stopwatch stopWatch = Stopwatch.createStarted();
        taskItemService.executeShardingTasks(shardingContext.getShardingItem(), shardingContext.getShardingTotalCount());
        BusinessMonitor.recordOne("busi-dirverjob", stopWatch.elapsed(TimeUnit.MILLISECONDS));
    }
}
