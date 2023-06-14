

package com.ark.bcp.domain.datachannel.event.inspection;

import com.ark.bcp.domain.annotations.Reentrant;
import com.ark.bcp.domain.datachannel.BaseEventSource;
import com.ark.bcp.domain.datachannel.DynamicDataSource;
import com.ark.bcp.domain.entity.InspectionEventSourceEntity;
import com.missfresh.risk.bcp.enums.LoadDataStrategyDefine;
import io.elasticjob.lite.api.ShardingContext;
import io.elasticjob.lite.api.simple.SimpleJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import io.elasticjob.lite.spring.api.SpringJobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class DynamicInspectionEventSrouce extends BaseEventSource implements DynamicDataSource, SimpleJob {
    private final static Logger logger = LoggerFactory.getLogger(DynamicInspectionEventSrouce.class);

    protected static final int SHARDING_CNT = 1;
    protected static final String SHARDING_PARAM = "1=1";

    private ZookeeperRegistryCenter regCenter;

    private volatile InspectionSimpleJobFunction inspectionSimpleJobFunction;

    public DynamicInspectionEventSrouce(InspectionEventSourceEntity eventSourceConfigEntity, ZookeeperRegistryCenter regCenter) {
        super(eventSourceConfigEntity);
        this.regCenter = regCenter;
    }

    @Reentrant
    @Override
    public boolean initDataChannel() {
        if (!(getEventSourceConfigEntity() instanceof InspectionEventSourceEntity)) {
            return false;
        }
        InspectionEventSourceEntity iesEntity = (InspectionEventSourceEntity) getEventSourceConfigEntity();
        String cron = iesEntity.getCron();

        String jobName = "bcp_job_trigger_" + iesEntity.getId();

        // 注销job
        JobRegistry.getInstance().shutdown(jobName);
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(iesEntity.getLoadDataStrategy());
        InspectionSimpleJobFunction newFunction = InspectionSimpleJobFunctionFactory.create(loadDataStrategyDefine, iesEntity, this);
        InspectionSimpleJobFunction oldFunction = inspectionSimpleJobFunction;
        inspectionSimpleJobFunction = newFunction;
        if (null != oldFunction) {
            oldFunction.close();
            oldFunction = null;
        }

        // 重新注册job
        SpringJobScheduler springJobScheduler = new SpringJobScheduler(
                this,
                regCenter,
                getLiteJobConfiguration(
                        jobName,
                        this.getClass(),
                        cron,
                        SHARDING_CNT,
                        SHARDING_PARAM));
        springJobScheduler.init();
        return true;

    }

    private LiteJobConfiguration getLiteJobConfiguration(
            final String jobName,
            final Class<? extends SimpleJob> jobClass,
            final String cron,
            final int shardingTotalCount,
            final String shardingItemParameters) {
        return LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(
                        JobCoreConfiguration.newBuilder(
                                jobName,
                                cron, shardingTotalCount)
                                .shardingItemParameters(shardingItemParameters).build(),
                        jobClass.getCanonicalName())).overwrite(true).build();
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    /**
     * 执行作业.
     *
     * @param shardingContext 分片上下文
     */
    @Override
    final public void execute(ShardingContext shardingContext) {

        InspectionEventSourceEntity iesEntity = (InspectionEventSourceEntity) getEventSourceConfigEntity();
        if (null == iesEntity || !iesEntity.isEnable()) {
            logger.info("job被停止:{}", null != iesEntity ? iesEntity.getId() : null);
            return;
        }
        if (null != inspectionSimpleJobFunction) {
            inspectionSimpleJobFunction.execute(shardingContext);
        }
    }
}
