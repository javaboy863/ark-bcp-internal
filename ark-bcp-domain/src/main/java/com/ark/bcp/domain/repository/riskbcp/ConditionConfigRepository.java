
package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.ConditionConfigEntity;

import java.util.List;


/**
 * 
 **/
public interface ConditionConfigRepository {

    /**
     * 获取条件信息
     * @param id
     * @return
     */
    ConditionConfigEntity selectConditionConfigById(Long id);

    /**
     * 插入新的条件
     * @param bcpConditionConfig
     * @return
     */
    int insertConditionConfig(ConditionConfigEntity bcpConditionConfig);

    /**
     * 按照id更细一个条件
     * @param bcpConditionConfig
     * @return
     */
    int updateConditionConfigByIdSelective(ConditionConfigEntity bcpConditionConfig);

    /**
     * 获取条件列表总数
     * @param bcpConditionConfig
     * @return
     */
    Long selectBcpConditionConfigListPageTotal(ConditionConfigEntity bcpConditionConfig);

    /**
     * 获取条件分页
     * @param bcpConditionConfig
     * @return
     */
    List<ConditionConfigEntity> selectBcpConditionConfigListPage(ConditionConfigEntity bcpConditionConfig);

    /**
     * 获取所有条件
     * @param bcpConditionConfig
     * @return
     */
    List<ConditionConfigEntity> selectConditionConfigList(ConditionConfigEntity bcpConditionConfig);

}
