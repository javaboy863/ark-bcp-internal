package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSON;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.domain.entity.EventMessageEntity;
import com.missfresh.risk.bcp.domain.service.DecisionService;
import com.missfresh.risk.bcp.domain.vo.EventMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 */
@RestController
@RequestMapping("/risk/bcp/event")
@Slf4j
public class EventUploadController {


    @Resource
    private DecisionService decisionService;

    @PostMapping("/upload")
    @ResponseBody
    public Result receiveEventMsg(@RequestBody EventMessageVO eventMsgVO) {
        log( "收到Http上报的事件消息，messageVO:{}",eventMsgVO);
        //检查实体
        checkEventMessage(eventMsgVO);
        //构建EventMessageEntity
        EventMessageEntity eventMsg = buildEventMessageEntity(eventMsgVO, eventMsgVO.getSourceId());
        //处理事件决策.
        decisionService.decision(eventMsgVO.getSourceId(), eventMsg);
        return Result.wrapSuccess(null);
    }

    private void log( String s,Object o) {
        log.info(s, o);
    }

    private void checkEventMessage(EventMessageVO eventMsgVO) {
        if (StringUtils.isNotBlank(eventMsgVO.getMessageId())) {
            return;
        }
        eventMsgVO.setMessageId(UUID.randomUUID().toString());
        log( "message id is null, generate:{}",eventMsgVO);
    }

    private EventMessageEntity buildEventMessageEntity(EventMessageVO eventMsgVO, Long sourceId) {
        EventMessageEntity eventMsg = EventMessageEntity.builder()
                .dataSourceId(sourceId)
                .messageId(eventMsgVO.getMessageId())
                .messageBody(JSON.parseObject(eventMsgVO.getMessageBody()))
                .rawBody(eventMsgVO.getMessageBody())
                .receiveTime(new Date())
                .build();
        return eventMsg;
    }
}
