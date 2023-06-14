package com.ark.bcp.domain.service;

import com.ark.bcp.domain.entity.CheckFailRecordEntity;
import com.ark.bcp.domain.repository.riskbcp.CheckFailRecordRepository;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 */
@Slf4j
@Service
public class CheckFailRecordService {

    @Resource
    private CheckFailRecordRepository checkFailRecordRepository;

    public CheckFailRecordEntity getById(Long id) {
        return checkFailRecordRepository.selectById(id);
    }

    public List<CheckFailRecordEntity> getByRuleId(Integer ruleId) {
        List<CheckFailRecordEntity> entities =  checkFailRecordRepository.selectByRuleId(ruleId);
        if (!CollectionUtils.isEmpty(entities)) {
            entities.forEach(CheckFailRecordEntity::unzipEventMessage);
        }
        return entities;
    }

    public int add(CheckFailRecordEntity entity) {
        if (entity == null) {
            log.error("新增校验失败记录，入参不能为null");
            return 0;
        }
        entity.zipEventMessage();
        return checkFailRecordRepository.insert(entity);
    }

    public int addSelective(CheckFailRecordEntity entity) {
        if (entity == null) {
            log.error("新增校验失败记录，入参不能为null");
            return 0;
        }
        entity.zipEventMessage();
        return checkFailRecordRepository.insertSelective(entity);
    }


    public int updateBcpCheckFailRecordByIdSelective(CheckFailRecordEntity bcpCheckFailRecord) {
        if (null == bcpCheckFailRecord || null == bcpCheckFailRecord.getId() || 0 == bcpCheckFailRecord.getId()) {
            return 0;
        }
        bcpCheckFailRecord.zipEventMessage();
        return checkFailRecordRepository.updateBcpCheckFailRecordByIdSelective(bcpCheckFailRecord);
    }

    /**
     * 分页查询总数
     * @param bcpCheckFailRecord
     * @param fromTime
     * @param toTime
     * @return
     */
    public Long selectBcpCheckFailRecordListPageTotal(
            CheckFailRecordEntity bcpCheckFailRecord, Date fromTime, Date toTime) {
        if (null == bcpCheckFailRecord) {
            return 0L;
        }
        return checkFailRecordRepository.selectBcpCheckFailRecordListPageTotal(bcpCheckFailRecord, fromTime, toTime);
    }

    /**
     * 分页查询
     * @param bcpCheckFailRecord
     * @param fromTime 开始时间
     * @param toTime 结束时间
     * @return
     */
    public List<CheckFailRecordEntity> selectBcpCheckFailRecordListPage(
            CheckFailRecordEntity bcpCheckFailRecord, Date fromTime, Date toTime) {
        if (null == bcpCheckFailRecord) {
            return Lists.newArrayList();
        }
        List<CheckFailRecordEntity> entities = checkFailRecordRepository.selectBcpCheckFailRecordListPage(bcpCheckFailRecord, fromTime, toTime);
        if (!CollectionUtils.isEmpty(entities)) {
            entities.forEach(CheckFailRecordEntity::unzipEventMessage);
        }
        return entities;
    }

    public List<CheckFailRecordEntity> selectBcpCheckFailRecordList(
            CheckFailRecordEntity bcpCheckFailRecord) {
        if (null == bcpCheckFailRecord) {
            return Lists.newArrayList();
        }
        List<CheckFailRecordEntity> entities =  checkFailRecordRepository.selectBcpCheckFailRecordList(bcpCheckFailRecord);
        if (!CollectionUtils.isEmpty(entities)) {
            entities.forEach(CheckFailRecordEntity::unzipEventMessage);
        }
        return entities;
    }

}
