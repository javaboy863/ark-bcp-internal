package com.ark.bcp.domain.repository.riskbcp;

import org.springframework.stereotype.Repository;
import com.ark.bcp.domain.entity.EventTaskItemEntity;

import java.util.List;

/**
 */
@Repository
public interface EventTaskItemRepository {
    /**
     * 根据ID查询
     * @param id
     * @return
     */
    EventTaskItemEntity selectById(Long id);

    /**
     * 根据ID批量查询
     * @param ids
     * @return
     */
    List<EventTaskItemEntity> selectByIds(List<Long> ids);

    /**
     * 新增
     * @param taskItem
     * @return
     */
    int insert(EventTaskItemEntity taskItem);

    /**
     * 新增，只插入有值字段
     * @param taskItem
     * @return
     */
    int insertSelective(EventTaskItemEntity taskItem);

    /**
     * 按ID更新
     * @param taskItem
     * @return
     */
    int updateById(EventTaskItemEntity taskItem);

    /**
     * 按ID更新，只更新有值字段
     * @param taskItem
     * @return
     */
    int updateByIdSelective(EventTaskItemEntity taskItem);

    /**
     * 按ID删除，物理删除
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 根据ID批量查询
     * @param taskItem
     * @param maxPageSize
     * @return
     */
    List<Long> selectShardingEntityByPageSelective (EventTaskItemEntity taskItem);
}
