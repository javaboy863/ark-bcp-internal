

package com.ark.bcp.infr.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.missfresh.risk.bcp.domain.entity.EventMessageEntity;
import com.missfresh.risk.bcp.domain.infrservice.MessagePoolService;
import com.missfresh.shardingredis.command.Sedis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Service(value = "messagepool")
public class MessagePoolServiceImpl implements MessagePoolService {
    private static final Logger logger = LoggerFactory.getLogger(MessagePoolServiceImpl.class);

    @Resource(name = "message-pool-sedis")
    private Sedis sedis;


    @Override
    public void saveEventMessageToPool(String matchKey, EventMessageEntity<?> messageEntity) {
        if (StringUtils.isEmpty(matchKey) || null == messageEntity) {
            logger.info("保存消息到匹配池失败:{},{}", matchKey, JSON.toJSONString(messageEntity));
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", messageEntity.getMessageId());
            jsonObject.put("raw", messageEntity.getRawBody());
            String result = sedis.set(matchKey, jsonObject.toJSONString());
            sedis.exists(matchKey);
            logger.info("save event msg to pool result:{},{},{}", result, matchKey, JSON.toJSONString(messageEntity));
        } catch (Exception e) {
            logger.info("save event msg to pool fail:{},{}", matchKey, JSON.toJSONString(messageEntity));
        }
    }

    @Override
    public EventMessageEntity<?> readEventMessageFromPool(String matchKey) {
        if (StringUtils.isEmpty(matchKey)) {
            logger.info("读取消息到匹配池失败:{}", matchKey);
            return null;
        }
        try {
            String result = sedis.get(matchKey);
            if (org.springframework.util.StringUtils.isEmpty(result)) {
                logger.info("读取消息到匹配池失败:{}", matchKey);
                return null;
            }
            JSONObject data = JSON.parseObject(result);
            logger.info("read event msg to pool result:{},{}", result, matchKey);

            return EventMessageEntity.builder()
                    .messageId(data.getString("id"))
                    .rawBody(data.getString("raw"))
                    .build();
        } catch (Exception e) {
            logger.info("read event msg to pool fail:{}", matchKey);
        }
        return null;
    }
}
