package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.CheckRuleStatEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 */
@Repository
public interface CheckRuleStatRepository {

    /**
     * 按ID查询
     * @param id
     * @return
     */
    CheckRuleStatEntity selectById(Integer id);

    /**
     * 按规则ID查询
     * @param ruleId
     * @return
     */
    List<CheckRuleStatEntity> selectByRuleId(Integer ruleId);

    /**
     * 新增
     * @param ruleStat
     * @return
     */
    int insert(CheckRuleStatEntity ruleStat);

    /**
     * 新增，只插入有值字段
     * @param ruleStat
     * @return
     */
    int insertSelective(CheckRuleStatEntity ruleStat);

    /**
     * 更新
     * @param ruleStat
     * @return
     */
    int updateById(CheckRuleStatEntity ruleStat);

    /**
     *  更新，只更新有值字段
     * @param ruleStat
     * @return
     */
    int updateByIdSelective(CheckRuleStatEntity ruleStat);


    /**
     * 统计数量自增
     * @param ruleId
     * @param statTime
     * @param type 1:成功数，2:失败数，3:异常数
     * @return
     */
    int incrStatCount(@Param("ruleId") Integer ruleId, @Param("statTime") Integer statTime, @Param("type") int type);
}
