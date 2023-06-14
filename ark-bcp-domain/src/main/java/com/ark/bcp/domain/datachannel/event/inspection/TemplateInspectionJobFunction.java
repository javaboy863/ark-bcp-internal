

package com.ark.bcp.domain.datachannel.event.inspection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ark.bcp.domain.datachannel.channel.EventMessageListenner;
import com.ark.bcp.domain.datachannel.channel.factory.SqlDataSourceFactory;
import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.script.SqlExecuteInvoker;
import com.ark.bcp.domain.vo.LoadDataTemplateVO;
import io.elasticjob.lite.api.ShardingContext;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 */
public class TemplateInspectionJobFunction implements InspectionSimpleJobFunction {

    private final static Logger logger = LoggerFactory.getLogger(TemplateInspectionJobFunction.class);

    private Map<String, DataSource> dataSourceMap = Maps.newConcurrentMap();

    private List<LoadDataTemplateVO> loadDataTemplateVOList;

    private EventMessageListenner eventMessageListenner;

    public TemplateInspectionJobFunction(List<LoadDataTemplateVO> loadDataTemplateVOList, EventMessageListenner eventMessageListenner) {
        this.loadDataTemplateVOList = loadDataTemplateVOList;
        this.eventMessageListenner = eventMessageListenner;
        init();
    }

    /**
     * 执行作业.
     *
     * @param shardingContext 分片上下文
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        if (CollectionUtils.isEmpty(loadDataTemplateVOList)) {
            return;
        }

        Map<String, Object> results = Maps.newConcurrentMap();
        CountDownLatch countDownLatch = new CountDownLatch(loadDataTemplateVOList.size());
        for (LoadDataTemplateVO loadDataTemplateVO : loadDataTemplateVOList) {
            CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataSource dataSource = dataSourceMap.get(loadDataTemplateVO.getField());
                        List<JSONObject> result = SqlExecuteInvoker.execute(dataSource, loadDataTemplateVO.getSql());
                        results.put(loadDataTemplateVO.getField(), result);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await(3000, TimeUnit.MILLISECONDS);
            EventMessageEntity<?> eventMessageEntity = EventMessageEntity.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageBody(JSON.parseObject(JSON.toJSONString(results)))
                    .rawBody(JSON.toJSONString(results))
                    .receiveTime(new Date())
                    .build();
            eventMessageListenner.onMesssage(eventMessageEntity);
        } catch (Exception e) {
            logger.error("混合查询线程 countDownLatch 等待异常", e);
        }
    }

    private void init() {
        try {
            Map<String, DataSource> oldDataSourceMap = dataSourceMap;
            Map<String, DataSource> newDataSourceMap = batchCreateDataSrouce(loadDataTemplateVOList);
            sweepDataSourceMap(oldDataSourceMap, newDataSourceMap);
        } catch (Exception e) {
            logger.error("加载数据源异常", e);
        }
    }

    private void sweepDataSourceMap(final Map<String, DataSource> oldDataSourceMap, final Map<String, DataSource> newDataSourceMap) {
        dataSourceMap = newDataSourceMap;
        if (null != dataSourceMap && null != oldDataSourceMap) {
            dataSourceMap.forEach((s, dataSource) -> {
                oldDataSourceMap.remove(s);
            });

            oldDataSourceMap.forEach((s, dataSource) -> {
                dataSource.close();
            });
            oldDataSourceMap.clear();
        }
    }

    private static Map<String, DataSource> batchCreateDataSrouce(final List<LoadDataTemplateVO> loadDataTemplates) {
        if (CollectionUtils.isEmpty(loadDataTemplates)) {
            return null;
        }
        Map<String, DataSource> newDataSourceMap = Maps.newConcurrentMap();

        try {
            for (LoadDataTemplateVO loadDataTemplateVO : loadDataTemplates) {
                SqlDataSourceFactory.SqlConnProperties sqlConnProperties = SqlDataSourceFactory.SqlConnProperties.builder()
                        .host(loadDataTemplateVO.getHost())
                        .port(Integer.valueOf(loadDataTemplateVO.getPort()))
                        .database(loadDataTemplateVO.getDatabase())
                        .usr(loadDataTemplateVO.getUsr())
                        .pwd(loadDataTemplateVO.getPwd())
                        .build();
                DataSource dataSource = SqlDataSourceFactory.createSqlDataSource(sqlConnProperties);
                newDataSourceMap.put(loadDataTemplateVO.getField(), dataSource);
            }
        } catch (Exception e) {
            logger.error("初始化连接异常", e);
        }
        return newDataSourceMap;
    }

    @Override
    public void close() {
        if (!CollectionUtils.isEmpty(dataSourceMap)) {
            dataSourceMap.forEach((s, dataSource) -> {
                dataSource.close();
            });
            dataSourceMap.clear();
        }
    }
}
