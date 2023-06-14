package com.ark.bcp.domain.service;

import com.ark.bcp.domain.entity.CheckRuleStatEntity;
import com.ark.bcp.domain.repository.riskbcp.CheckRuleStatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 */
@Slf4j
@Service
public class CheckRuleStatService {

    @Resource
    private CheckRuleStatRepository checkRuleStatRepository;

    public CheckRuleStatEntity getById(Integer id) {
        return checkRuleStatRepository.selectById(id);
    }

    public List<CheckRuleStatEntity> getByRuleId(Integer ruleId) {
        return checkRuleStatRepository.selectByRuleId(ruleId);
    }

    public int add(CheckRuleStatEntity statEntity) {
        log.info("新增校验规则统计项，statEntity:{}", statEntity);
        if (statEntity == null) {
            log.error("新增校验规则统计项，入参不能为null");
            return 0;
        }
        return checkRuleStatRepository.insert(statEntity);
    }

    public int addSelective(CheckRuleStatEntity statEntity) {
        log.info("新增校验规则统计项，statEntity:{}", statEntity);
        if (statEntity == null) {
            log.error("新增校验规则统计项，入参不能为null");
            return 0;
        }
        return checkRuleStatRepository.insertSelective(statEntity);
    }

    public int updateById(CheckRuleStatEntity statEntity) {
        log.info("更新校验规则统计项，statEntity:{}", statEntity);
        if (statEntity == null || statEntity.getId() == null) {
            log.error("更新校验规则统计项，入参有误");
            return 0;
        }
        return checkRuleStatRepository.updateById(statEntity);
    }

    public int updateByIdSelective(CheckRuleStatEntity statEntity) {
        log.info("更新校验规则统计项，statEntity:{}", statEntity);
        if (statEntity == null || statEntity.getId() == null) {
            log.error("更新校验规则统计项，入参有误");
            return 0;
        }
        return checkRuleStatRepository.updateByIdSelective(statEntity);
    }

    /**
     * 增加成功执行数量
     * @param ruleId
     * @param statTime
     * @return
     */
    public int incrSuccCount(Integer ruleId, Integer statTime) {
        return checkRuleStatRepository.incrStatCount(ruleId, statTime, 1);
    }

    /**
     * 增加失败执行数量
     * @param ruleId
     * @param statTime
     * @return
     */
    public int incrFailCount(Integer ruleId, Integer statTime) {
        return checkRuleStatRepository.incrStatCount(ruleId, statTime, 2);
    }

    /**
     * 增加异常执行数量
     * @param ruleId
     * @param statTime
     * @return
     */
    public int incrExceptionCount(Integer ruleId, Integer statTime) {
        return checkRuleStatRepository.incrStatCount(ruleId, statTime, 3);
    }
}
