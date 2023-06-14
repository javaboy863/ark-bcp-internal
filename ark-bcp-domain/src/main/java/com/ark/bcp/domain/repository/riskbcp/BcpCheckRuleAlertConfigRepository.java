
package com.ark.bcp.domain.repository.riskbcp;


import com.ark.bcp.domain.entity.BcpCheckRuleAlertEntity;

import java.util.List;

/**
 **/
public interface BcpCheckRuleAlertConfigRepository {

    /**
     * 按照id查找规则的报警配置信息
     *
     * @param id
     * @return
     */
    BcpCheckRuleAlertEntity selectBcpCheckRuleAlertConfigById(Long id);

    /**
     * 插入规则的报警配置
     *
     * @param bcpCheckRuleAlertConfig
     * @return
     */
    int insertBcpCheckRuleAlertConfig(BcpCheckRuleAlertEntity bcpCheckRuleAlertConfig);

    /**
     * 更新规则的报警配置
     *
     * @param bcpCheckRuleAlertConfig
     * @return
     */
    int updateBcpCheckRuleAlertConfigBySelective(BcpCheckRuleAlertEntity bcpCheckRuleAlertConfig);

    /**
     * 删除规则的报警配置
     *
     * @param ruleId
     * @return
     */
    int deleteBcpCheckRuleAlertConfigByRuleId(Long ruleId);

    /**
     * 查找所有规则报警配置
     *
     * @param bcpCheckRuleAlertConfig
     * @return
     */
    List<BcpCheckRuleAlertEntity> selectBcpCheckRuleAlertConfigList(BcpCheckRuleAlertEntity bcpCheckRuleAlertConfig);

}
