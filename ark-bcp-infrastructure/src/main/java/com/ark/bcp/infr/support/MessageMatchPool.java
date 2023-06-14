

package com.ark.bcp.infr.support;

import com.alibaba.fastjson.JSONObject;
import com.missfresh.risk.bcp.domain.entity.EventMessageEntity;
import com.missfresh.risk.bcp.domain.infrservice.MessagePoolService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.util.MapContextFormator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 */
public class MessageMatchPool extends AbstractApplicationContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(MessageMatchPool.class);

    public static JSONObject readMessageByMatchKey(String matchKeyTemplete, Map<String, Object> rawParam) {
        String realKey = MapContextFormator.formatRealTempleteKey(matchKeyTemplete, rawParam);
        logger.info("get data from realkkey:{}", realKey);
        if (StringUtils.isEmpty(realKey)) {
            return null;
        }

        MessagePoolService messagePoolService = getExtension(MessagePoolService.class, "MessagePoolService");
        if (null == messagePoolService) {
            return null;
        }
        EventMessageEntity<?> entity = messagePoolService.readEventMessageFromPool(realKey);
        if (null == entity) {
            return null;
        }
        return JSONObject.parseObject(entity.getRawBody());
    }
}
