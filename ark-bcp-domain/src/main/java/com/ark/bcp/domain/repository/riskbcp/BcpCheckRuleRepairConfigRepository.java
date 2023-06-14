package com.ark.bcp.domain.repository.riskbcp;


import com.ark.bcp.domain.entity.BcpCheckRuleRepairEntity;

import java.util.List;

/**
 **/
public interface BcpCheckRuleRepairConfigRepository {

    /**
     * 查找规则的修复配置信息
     *
     * @param id
     * @return
     */
    BcpCheckRuleRepairEntity selectBcpCheckRuleRepairConfigById(Long id);

    /**
     * 保存规则的配置修复信息
     *
     * @param bcpCheckRuleRepairConfig
     * @return
     */
    int insertBcpCheckRuleRepairConfig(BcpCheckRuleRepairEntity bcpCheckRuleRepairConfig);

    /**
     * 更新单个规则的配置修复信息
     *
     * @param bcpCheckRuleRepairConfig
     * @return
     */
    int updateBcpCheckRuleRepairConfigBySelective(BcpCheckRuleRepairEntity bcpCheckRuleRepairConfig);

    /**
     * 删除贵的的配置修复信息
     *
     * @param ruleId
     * @return
     */
    int deleteBcpCheckRuleRepairConfigByRuleId(Long ruleId);

    /**
     * 查找规则配置修复信息列表
     *
     * @param bcpCheckRuleRepairConfig
     * @return
     */
    List<BcpCheckRuleRepairEntity> selectBcpCheckRuleRepairConfigList(BcpCheckRuleRepairEntity bcpCheckRuleRepairConfig);
}
