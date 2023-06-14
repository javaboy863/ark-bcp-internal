package com.ark.bcp.domain.service;

import com.ark.bcp.domain.repository.riskbcp.CheckRuleConfigRepository;
import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 添加校验规则领域服务.
 */
@Slf4j
@Service
public class RuleConfigDomainService {

    @Resource
    private CheckRuleConfigRepository checkRuleConfigRepository;

    public void addRuleConfig(CheckRuleConfigEntity ruleConfigEntity) {
        log.info("addRuleConfig:{}", ruleConfigEntity);
        checkRuleConfigRepository.insertCheckRuleConfig(ruleConfigEntity);
    }

    public Long queryRuleConfigListPageTotal(CheckRuleConfigEntity condition) {
        return checkRuleConfigRepository.selectCheckRuleConfigListPageTotal(condition);
    }

    public List<CheckRuleConfigEntity> queryByPage(CheckRuleConfigEntity condition) {
        return checkRuleConfigRepository.selectCheckRuleConfigListPage(condition);
    }

    public void update(CheckRuleConfigEntity ruleConfigEntity) {
        log.info("update config:{}", ruleConfigEntity);
        checkRuleConfigRepository.updateCheckRuleConfigByIdSelective(ruleConfigEntity);
    }

    public List<CheckRuleConfigEntity> selectCheckRuleConfigList(CheckRuleConfigEntity ruleConfigEntity) {
        return checkRuleConfigRepository.selectCheckRuleConfigList(ruleConfigEntity);
    }

    /**
     * "" .
     *
     * @param id .
     * @return "".
     */
    public CheckRuleConfigEntity selectCheckRuleConfigById(Long id) {
        return checkRuleConfigRepository.selectCheckRuleConfigById(id);
    }

    public List<CheckRuleConfigEntity> searchCheckRuleConfigList(CheckRuleConfigEntity ruleConfigEntity) {
        ruleConfigEntity.setPageSize(CheckRuleConfigEntity.DEFAULT_PAGE_SIZE);
        ruleConfigEntity.setRuleName("%" + ruleConfigEntity.getRuleName() + "%");
        return checkRuleConfigRepository.searchCheckRuleConfigList(ruleConfigEntity);
    }
}
