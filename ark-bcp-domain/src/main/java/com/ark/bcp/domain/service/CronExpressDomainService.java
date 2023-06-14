

package com.ark.bcp.domain.service;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.domain.cron.CronFieldParser;
import com.ark.bcp.domain.cron.CronFieldParserEnumable;
import com.ark.bcp.domain.cron.CronFieldParserEveryOne;
import com.ark.bcp.domain.cron.CronFieldParserRange;
import com.ark.bcp.domain.cron.CronFieldParserRepeat;
import com.ark.bcp.domain.entity.CronFieldEntity;
import com.ark.bcp.domain.exception.FailfastException;
import com.ark.bcp.domain.vo.cron.CronStrategyVO;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.missfresh.risk.bcp.domain.cron.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 */
@Service
public class CronExpressDomainService {

    private final static Logger logger = LoggerFactory.getLogger(CronExpressDomainService.class);

    /**
     * 秒，分，时，日，月 （周，年）
     */
    private final static Integer MIN_INVALIDATE_FIELD_COUNT = 5;

    private final static Map<CronStrategyVO, CronFieldParser> CRON_FIELD_PARSERS = Maps.newHashMap();

    static {
        CRON_FIELD_PARSERS.put(CronStrategyVO.EVERY_ONE, new CronFieldParserEveryOne());
        CRON_FIELD_PARSERS.put(CronStrategyVO.RANGE, new CronFieldParserRange());
        CRON_FIELD_PARSERS.put(CronStrategyVO.REPEAT, new CronFieldParserRepeat());
        CRON_FIELD_PARSERS.put(CronStrategyVO.ENUMABLE, new CronFieldParserEnumable());
    }

    /**
     * 拼接成cron 表达式
     *
     * @param cronFieldEntities
     * @return
     */
    public String makeCronString(final List<CronFieldEntity> cronFieldEntities) {
        if (StringUtils.isEmpty(cronFieldEntities) || cronFieldEntities.size() < MIN_INVALIDATE_FIELD_COUNT) {
            throw new FailfastException(null, "有效字段过少");
        }
        StringBuilder cronBuilder = new StringBuilder();
        for (CronFieldEntity cronFieldEntity : cronFieldEntities) {
            if (null == cronFieldEntity) {
                throw new FailfastException(null, "有效字段为空");
            }
            CronFieldParser parser = CRON_FIELD_PARSERS.get(cronFieldEntity.getStrategy());
            if (null == parser) {
                // 一个字段解析不出来即视为错误
                throw new FailfastException(null, "无效的字段类型");
            }
            String subCron = parser.makeCronExpress(cronFieldEntity);
            if (StringUtils.isEmpty(subCron)) {
                // 一个字段解析不出来即视为错误
                throw new FailfastException(null, "生成字段错误:"+ JSON.toJSONString(cronFieldEntity));
            }
            cronBuilder.append(subCron).append(" ");
        }
        // 周字段写死了不允许修改
        cronBuilder.append("?");
        return cronBuilder.toString();
    }

    public List<CronFieldEntity> parseCronString(final String cronExpress) {
        if (StringUtils.isEmpty(cronExpress)) {
            return null;
        }
        List<String> subCrons = Splitter.on(" ").splitToList(cronExpress);
        if (CollectionUtils.isEmpty(subCrons) || subCrons.size() < MIN_INVALIDATE_FIELD_COUNT) {
            logger.info("cron表达式不合法:{}", cronExpress);
            return null;
        }
        List<CronFieldEntity> entities = Lists.newArrayList();
        for (int i = 0; i < MIN_INVALIDATE_FIELD_COUNT; i++) {
            CronFieldEntity entity = parseSubCron(subCrons.get(i));
            if (null == entity) {
                return null;
            }
            entities.add(entity);
        }
        return entities;
    }

    private CronFieldEntity parseSubCron(final String subCron) {
        for (Map.Entry<CronStrategyVO, CronFieldParser> entry : CRON_FIELD_PARSERS.entrySet()) {
            CronFieldEntity entity = entry.getValue().parseCronExpress(subCron);
            if (null != entity) {
                return entity;
            }
        }
        return null;
    }
}
