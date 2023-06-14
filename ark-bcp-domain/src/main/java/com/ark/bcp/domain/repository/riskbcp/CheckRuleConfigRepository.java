package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.CheckRuleConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 **/
@Repository
public interface CheckRuleConfigRepository {
    /**
     * 根据id获取规则配置信息.
     *
     * @param id ""
     * @return ""
     */
    CheckRuleConfigEntity selectCheckRuleConfigById(Long id);

    /**
     * 插入规则信息.
     *
     * @param bcpCheckRuleConfig ""
     * @return ""
     */
    int insertCheckRuleConfig(CheckRuleConfigEntity bcpCheckRuleConfig);

    /**
     * 按照id更新规则配置.
     *
     * @param bcpCheckRuleConfig ""
     * @return ""
     */
    int updateCheckRuleConfigByIdSelective(CheckRuleConfigEntity bcpCheckRuleConfig);

    /**
     * 筛选总数，用于分页.
     *
     * @param bcpCheckRuleConfig ""
     * @return ""
     */
    Long selectCheckRuleConfigListPageTotal(CheckRuleConfigEntity bcpCheckRuleConfig);

    /**
     * 分页查询.
     *
     * @param bcpCheckRuleConfig ""
     * @return ""
     */
    List<CheckRuleConfigEntity> selectCheckRuleConfigListPage(CheckRuleConfigEntity bcpCheckRuleConfig);

    /**
     * 精确查询规则列表
     * @param bcpCheckRuleConfig
     * @return
     */
    List<CheckRuleConfigEntity> selectCheckRuleConfigList(CheckRuleConfigEntity bcpCheckRuleConfig);

    /**
     * 模糊查找生效中的规则id
     * @param checkRuleConfigEntity
     * @return
     */
    List<CheckRuleConfigEntity> searchCheckRuleConfigList(CheckRuleConfigEntity checkRuleConfigEntity);
}
