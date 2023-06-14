package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wanghetao on 2020-09-03.
 */
@Repository
public interface EventSourceConfigRepository {
    /**
     * 按ID查询.
     *
     * @param id ""
     * @return ""
     */
    EventSourceConfigEntity selectById(Long id);

    /**
     * 按ID批量查询.
     *
     * @param ids ""
     * @return ""
     */
    List<EventSourceConfigEntity> selectByIds(List<Integer> ids);

    /**
     * 按条件查询
     * @param queryCond
     * @return
     */
    List<EventSourceConfigEntity> queryByCondition(EventSourceConfigEntity queryCond);

    /**
     * 查询符合条件的事件源ID
     * @param queryCond
     * @return
     */
    List<Long> queryIdsByCondition(EventSourceConfigEntity queryCond);


    /**
     * 分页查询
     * @param condition
     * @return
     */
    List<EventSourceConfigEntity> selectListPage(EventSourceConfigEntity condition);


    /**
     * 新增事件源配置，只插入有值字段.
     *
     * @param eventSourceConfig ""
     * @return ""
     */
    int insertSelective(EventSourceConfigEntity eventSourceConfig);

    /**
     * 更新事件源配置，只更新有值字段.
     *
     * @param eventSourceConfig ""
     * @return ""
     */
    int updateByIdSelective(EventSourceConfigEntity eventSourceConfig);

    /**
     * 删除数据源配置，逻辑删除.
     *
     * @param id ""
     * @return ""
     */
    int deleteById(Integer id);

    /**
     * 分页查询总条数
     * @param condition
     * @return
     */
    Long pageTotal(EventSourceConfigEntity condition);

    /**
     * 按照名字查询事件源
     * @param name
     * @return
     */
    List<EventSourceConfigEntity> queryByName(String name);
}
