package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.CheckFailRecordEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 */
@Repository
public interface CheckFailRecordRepository {
    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    CheckFailRecordEntity selectById(Long id);

    /**
     * 根据ruleId查询
     *
     * @param ruleId
     * @return
     */
    List<CheckFailRecordEntity> selectByRuleId(Integer ruleId);

    /**
     * 新增
     *
     * @param checkFailRecord
     * @return
     */
    int insert(CheckFailRecordEntity checkFailRecord);

    /**
     * 新增，只插入有值字段.
     *
     * @param checkFailRecord ""
     * @return ""
     */
    int insertSelective(CheckFailRecordEntity checkFailRecord);


    /**
     * 更新.
     *
     * @param checkFailRecord ""
     * @return ""
     */
    int updateBcpCheckFailRecordByIdSelective(CheckFailRecordEntity checkFailRecord);

    /**
     * 获取异常记录总数，用于分页.
     *
     * @param checkFailRecord ""
     * @param fromTime        ""
     * @param endTime         ""
     * @return ""
     */
    Long selectBcpCheckFailRecordListPageTotal(
            @Param("item") CheckFailRecordEntity checkFailRecord,
            @Param("fromTime") Date fromTime,
            @Param("toTime") Date endTime
    );

    /**
     * 获取异常记录分页.
     *
     * @param checkFailRecord ""
     * @param fromTime        ""
     * @param endTime         ""
     * @return ""
     */
    List<CheckFailRecordEntity> selectBcpCheckFailRecordListPage(
            @Param("item") CheckFailRecordEntity checkFailRecord,
            @Param("fromTime") Date fromTime,
            @Param("toTime") Date endTime);

    /**
     * 获取异常记录列表.
     *
     * @param checkFailRecord ""
     * @return ""
     */
    List<CheckFailRecordEntity> selectBcpCheckFailRecordList(CheckFailRecordEntity checkFailRecord);

}
