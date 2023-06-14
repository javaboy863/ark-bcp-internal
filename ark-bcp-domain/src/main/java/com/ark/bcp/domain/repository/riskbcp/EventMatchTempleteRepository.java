
package com.ark.bcp.domain.repository.riskbcp;

import com.ark.bcp.domain.entity.EventMatchTemplateEntity;

/**
 */
public interface EventMatchTempleteRepository {
    /**
     * 获取匹配模版
     * @param id
     * @return
     */
    EventMatchTemplateEntity selectById(Integer id);

    /**
     * 按照规则获取匹配模版
     * @param eventSourceId
     * @return
     */
    EventMatchTemplateEntity selectByEventSourceId(Long eventSourceId);

    /**
     * 保存匹配模版
     * @param entity
     * @return
     */
    int insert(EventMatchTemplateEntity entity);

    /**
     * 更新匹配模版
     * @param entity
     * @return
     */
    int updateByEventSourceSelective(EventMatchTemplateEntity entity);
}
