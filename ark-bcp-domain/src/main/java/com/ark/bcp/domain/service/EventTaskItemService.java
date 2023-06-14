package com.ark.bcp.domain.service;

import com.ark.bcp.domain.repository.riskbcp.EventTaskItemRepository;
import com.ark.bcp.domain.entity.EventTaskItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 */
@Slf4j
@Service
@Configuration
public class EventTaskItemService {
    /**
     * 最小页码
     */
    private final static int MIN_PAEG_SIZE = 1;
    /**
     * 最大页码
     */
    private final static int MAX_PAEG_SIZE = 100;
    @Resource
    private EventTaskItemRepository eventTaskItemRepository;


    public EventTaskItemEntity getById(Long id) {
        return eventTaskItemRepository.selectById(id);
    }

    public List<EventTaskItemEntity> getByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("根据ID批量查询事件task，id列表为空");
            return Collections.emptyList();
        }
        return eventTaskItemRepository.selectByIds(ids);
    }

    public int add(EventTaskItemEntity taskItem) {
        log.info("新增事件执行task，taskItem:{}", taskItem);
        if (taskItem == null) {
            log.error("新增事件执行task不能为null");
            return 0;
        }
        return eventTaskItemRepository.insert(taskItem);
    }

    public int addSelective(EventTaskItemEntity taskItem) {
        log.info("新增事件执行task，taskItem:{}", taskItem);
        if (taskItem == null) {
            log.error("新增事件执行task不能为null");
            return 0;
        }
        return eventTaskItemRepository.insertSelective(taskItem);
    }

    public int updateById(EventTaskItemEntity taskItem) {
        log.info("更新事件执行task，taskItem:{}", taskItem);
        if (taskItem == null || taskItem.getId() == null) {
            log.error("更新事件执行task，入参有误");
            return 0;
        }
        return eventTaskItemRepository.updateById(taskItem);
    }

    public int updateByIdSelective(EventTaskItemEntity taskItem) {
        log.info("更新事件执行task，taskItem:{}", taskItem);
        if (taskItem == null || taskItem.getId() == null) {
            log.error("更新事件执行task，入参有误");
            return 0;
        }
        return eventTaskItemRepository.updateByIdSelective(taskItem);
    }

    @SuppressWarnings("AlibabaRemoveCommentedCode")
    public List<Long> selectShardingEntityByPageSelective(EventTaskItemEntity taskItem, int pageSize) {
        //[1,100]
        int maxSize = Math.min(Math.max(MIN_PAEG_SIZE, pageSize), MAX_PAEG_SIZE);
        taskItem.setMaxPageSize(maxSize);
        return eventTaskItemRepository.selectShardingEntityByPageSelective(taskItem);
    }


    /**
     * 按ID删除，物理删除
     *
     * @param id
     * @return
     */
    public int deleteById(Long id) {
        return eventTaskItemRepository.deleteById(id);
    }


}
