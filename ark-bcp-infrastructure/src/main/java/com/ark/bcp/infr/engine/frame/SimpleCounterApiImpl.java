
package com.ark.bcp.infr.engine.frame;

import com.google.common.collect.Lists;
import com.missfresh.risk.bcp.domain.infrservice.SimpleCounterService;
import com.missfresh.risk.bcp.domain.util.TimeUnitEnum;
import com.missfresh.shardingredis.command.Sedis;
import com.missfresh.shardingredis.protocol.jedis.clients.jedis.Tuple;
import com.mryx.monitor.api.BusinessMonitor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 */
@Service
public class SimpleCounterApiImpl implements SimpleCounterService {

    @Resource(name = "simplecount-sedis")
    private Sedis counterServer;

    private static final String BIZ_NAME = "alert:";

    private String wrapKey(String key) {
        return BIZ_NAME + key;
    }


    private void slideWindowIfNeed(String key, long currentTs, long intervalInMs) {
        long endTm = currentTs - intervalInMs;
        // 获取第一个元素信息
        Set<Tuple> tuples = counterServer.zrangeWithScores(key, 0, 0);
        if (!CollectionUtils.isEmpty(tuples)) {
            double scroe = tuples.iterator().next().getScore();
            BusinessMonitor.recordOne(String.format("busi-couter_slide_%s", scroe < endTm));
            if (scroe < endTm) {
                // 在时间窗口之外，需要推动窗口
                counterServer.zremrangeByScore(key, 0, endTm);
            }
        }
    }

    @Override
    public void add(String key, String member, TimeUnitEnum timeUnit, long interval) {
        add(key, member, System.currentTimeMillis(), timeUnit, interval);
    }

    /**
     * 个数添加.
     *
     * @param key
     * @param member
     * @param tmstamp
     * @param timeUnit
     * @param interval
     */
    @Override
    public void add(String key, String member, Long tmstamp, TimeUnitEnum timeUnit, long interval) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(member) || null == timeUnit) {
            return;
        }
        key = wrapKey(key);

        int unitSecond = timeUnit.getUnitSecond();
        long intervalInMs = unitSecond * interval * 1000;

        counterServer.zadd(key, tmstamp, member);
        counterServer.pexpire(key, intervalInMs);
        slideWindowIfNeed(key, tmstamp, intervalInMs);
    }

    @Override
    public long getWordCount(String key, TimeUnitEnum timeUnit, long interval) {
        if (StringUtils.isEmpty(key)) {
            return 0;
        }
        int unitSecond = timeUnit.getUnitSecond();
        long intervalInMs = unitSecond * interval * 1000;

        key = wrapKey(key);

        // 推动滑动窗口
        long tmstamp = System.currentTimeMillis();
        slideWindowIfNeed(key, tmstamp, intervalInMs);

        Long wordCount = counterServer.zcard(key);
        return null == wordCount ? 0 : wordCount;

    }

    @Override
    public List<String> getWordCountValue(String key, TimeUnitEnum timeUnit, long interval) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        int unitSecond = timeUnit.getUnitSecond();
        long intervalInMs = unitSecond * interval * 1000;

        key = wrapKey(key);

        // 推动滑动窗口
        long tmstamp = System.currentTimeMillis();
        slideWindowIfNeed(key, tmstamp, intervalInMs);

        Set<String> zrange = counterServer.zrange(key, 0, -1);
        return CollectionUtils.isEmpty(zrange) ? null : Lists.newArrayList(zrange);

    }

    @Override
    public void delWordCount(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        key = wrapKey(key);
        counterServer.del(key);
    }
}
