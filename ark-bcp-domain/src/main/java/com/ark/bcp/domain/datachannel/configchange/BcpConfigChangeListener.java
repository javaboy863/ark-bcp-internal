package com.ark.bcp.domain.datachannel.configchange;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.datachannel.EventSourceFactory;
import com.ark.bcp.domain.constant.EventSourceStatusEnum;

import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.ark.bcp.domain.engine.frame.rss.NotifyListener;
import com.ark.bcp.domain.util.AbstractApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Slf4j
@Service
public class BcpConfigChangeListener implements InitializingBean {

    @Resource
    private EventSourceFactory eventSourceFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        configChangeNotifier.subscribe("event_source_config_change", (String topic, String notifyStr) -> {
            // notifyStr为JSON字符串，格式为：{"id": <source_id>,  "status": <status>}
            log.info("接收到事件源变更的通知:{}", notifyStr);
            JSONObject notifyJson = JSON.parseObject(notifyStr);
            long sourceId = notifyJson.getLongValue("id");
            int status = notifyJson.getIntValue("status");
            if (status == EventSourceStatusEnum.CLOSE.getCode()) {
                log.info("接收到事件源变更的通知，关闭事件源，sourceId:{}", sourceId);
                eventSourceFactory.closeById(sourceId);
            } else if (status == EventSourceStatusEnum.OPEN.getCode()) {
                log.info("接收到事件源变更的通知，开启事件源，sourceId:{}", sourceId);
                eventSourceFactory.getById(sourceId);
            }
        });

        configChangeNotifier.subscribe("check_rule_config_change", (String topic, String notifyStr) -> {

        });

        configChangeNotifier.subscribe(Constant.BROADCAST_ID_EVENT, new NotifyListener() {
            @Override
            public void onNotify(String topic, String serializable) {
                log.info("recv sub change:{}, {}", topic, serializable);
            }
        });
    }
}
