package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.DynamicCodeConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 */
@Repository
@SuppressWarnings({"AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc"})
public interface DynamicCodeConfigRepository {
    /**
     * 获取脚本信息
     * @param id
     * @return
     */
    DynamicCodeConfigEntity selectBcpDynamicCodeConfigById(Long id);

    /**
     * 插入脚本
     * @param bcpDynamicCodeConfig
     * @return
     */
    int insertDynamicCodeConfig(DynamicCodeConfigEntity bcpDynamicCodeConfig);

    int updateDynamicCodeConfigByIdSelective(DynamicCodeConfigEntity bcpDynamicCodeConfig);

    Long selectBcpDynamicCodeConfigListPageTotal(DynamicCodeConfigEntity bcpDynamicCodeConfig);

    List<DynamicCodeConfigEntity> selectBcpDynamicCodeConfigListPage(DynamicCodeConfigEntity bcpDynamicCodeConfig);

    List<DynamicCodeConfigEntity> selectDynamicCodeConfigList(DynamicCodeConfigEntity bcpDynamicCodeConfig);
}
