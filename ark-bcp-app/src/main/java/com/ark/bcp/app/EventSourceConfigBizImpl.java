package com.ark.bcp.app;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.app.utils.EventSourceConfigEntitys;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.EventSourceConfigService;
import com.missfresh.risk.bcp.domain.constant.EventSourceStatusEnum;
import com.missfresh.risk.bcp.domain.constant.EventSourceTypeEnum;
import com.missfresh.risk.bcp.domain.engine.frame.ComponentTypeEnum;
import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.Constant;
import com.missfresh.risk.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.missfresh.risk.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.missfresh.risk.bcp.domain.entity.CheckRuleConfigEntity;
import com.missfresh.risk.bcp.domain.entity.EventMatchTemplateEntity;
import com.missfresh.risk.bcp.domain.entity.EventSourceConfigEntity;
import com.missfresh.risk.bcp.domain.exception.FailfastException;
import com.missfresh.risk.bcp.domain.service.EventSourceConfigDomainService;
import com.missfresh.risk.bcp.domain.service.RuleConfigDomainService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.util.MapContextFormator;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.domain.vo.DelayEventResourceConfigDO;
import com.missfresh.risk.bcp.dto.EventSourceConfigDto;
import com.missfresh.risk.bcp.dto.KafkaConnParamDto;
import com.missfresh.risk.bcp.dto.PageResultDto;
import com.missfresh.risk.bcp.dto.RocketMqConnParamDto;
import com.missfresh.risk.bcp.enums.DelayTypeDefine;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 */
@Service
@Slf4j
public class EventSourceConfigBizImpl implements EventSourceConfigService, InitializingBean {

    @Resource
    private EventSourceConfigDomainService eventSourceConfigDomainService;

    @Resource
    private RuleConfigDomainService ruleConfigDomainService;


    private void publishConfigChange(Long eventid, PublishParams.ActionType actionType) {
        PublishParams publishParams = new PublishParams();
        publishParams.setComponentTypeEnum(ComponentTypeEnum.EVENT);
        publishParams.setTypeId(String.valueOf(eventid));
        publishParams.setAction(actionType);
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        configChangeNotifier.doPublish(Constant.BROADCAST_ID_EVENT, JSON.toJSONString(publishParams));
        log.info("发送变更通知:{}", JSON.toJSONString(publishParams));
    }

    @Override
    public Result saveEventSource(EventSourceConfigDto eventSourceConfigDto) {
        logInfo("save eventSource:{}", eventSourceConfigDto);
        Result result = null;
        try {
            // 实体变更
            EventSourceConfigEntity entity = convertFromEventSourceConfigDto(eventSourceConfigDto);
            // 检查参数
            upsertEventSourceParamCheck(entity);
            int addResult = eventSourceConfigDomainService.add(entity);
            publishConfigChange(entity.getId(), PublishParams.ActionType.ADD);
            result = Result.wrapSuccess(addResult);
        } catch (FailfastException failfastException) {
            logError("保存事件错误:{}", failfastException.getMessage());
            return ResultUtils.wrapFailure(-1, failfastException.getMessage());
        } catch (Exception e) {
            logError("保存事件异常", e);
        } finally {
            logInfo("保存事件结果:{}", JSON.toJSONString(result));
        }
        return result;
    }

    private void upsertEventSourceParamCheck(EventSourceConfigEntity configEntity) {
        if (null != configEntity.getMatchTemplateEntity()) {
            if (0 != configEntity.getMatchTemplateEntity().getSaveToMatchDbFlag()
                    && !MapContextFormator.isHasPlaceHolder(configEntity.getMatchTemplateEntity().getTemplateName())) {
                throw new FailfastException(null, "匹配KEY应包含至少一组\"@xxx@\"占位符");
            }
        }
    }

    @Override
    public Result updateEventSource(EventSourceConfigDto eventSourceConfigDto) {
        log.info("update eventSource:{}", eventSourceConfigDto);
        Result result = null;
        try {
            // 实体变更
            EventSourceConfigEntity entity = convertFromEventSourceConfigDto(eventSourceConfigDto);
            // 检查参数
            upsertEventSourceParamCheck(entity);
            int updateResult = eventSourceConfigDomainService.updateByIdSelective(entity);
            publishConfigChange(entity.getId(), PublishParams.ActionType.UPDATE);
            result = Result.wrapSuccess(updateResult);
        } catch (FailfastException failfastException) {
            log.info("保存事件错误:{}", failfastException.getMessage());
            return ResultUtils.wrapFailure(-1, failfastException.getMessage());
        } catch (Exception e) {
            log.error("保存事件异常", e);
        } finally {
            log.info("保存事件结果:{}", JSON.toJSONString(result));
        }
        return result;
    }

    @Override
    public Result disable(EventSourceConfigDto eventSourceConfigDto) {
        List<Long> ruleConfigIdList = useEventRuleConfigIdList(eventSourceConfigDto);
        //check
        if (!ruleConfigIdList.isEmpty()) {
            return Result.wrapError(20001, "该事件正在被有效的规则:" + ruleConfigIdList.toString() + "使用, 不能禁用!");
        }
        //关闭或开启事件
        Result result = disableOrEnable(eventSourceConfigDto, EventSourceStatusEnum.CLOSE.getCode());
        //发送变更通知
        publishConfigChange(eventSourceConfigDto.getId(), PublishParams.ActionType.UPDATE);
        return result;
    }

    @Override
    public Result enable(EventSourceConfigDto eventSourceConfigDto) {
        Result result = disableOrEnable(eventSourceConfigDto, EventSourceStatusEnum.OPEN.getCode());
        //发送变更通知
        publishConfigChange(eventSourceConfigDto.getId(), PublishParams.ActionType.UPDATE);
        return result;
    }
    private void logError(String s, Object... o) {
        log.error(s, o);
    }


    private void logInfo(String s, Object... o) {
        log.info(s, o);
    }

    @Override
    public Result delate(EventSourceConfigDto eventSourceConfigDto) {
        //check parm
        if (!checkParm(eventSourceConfigDto)) {
            return Result.wrapSuccess(0);
        }
        List<Long> ruleConfigIdList = useEventRuleConfigIdList(eventSourceConfigDto);
        //check empty
        if (checkRule(ruleConfigIdList)){
            return Result.wrapError(20001, "该事件正在被有效的规则:" + ruleConfigIdList.toString() + "使用, 不能删除");
        }
        //buildEventSourceConfigEntity
        EventSourceConfigEntity entity = buildEventSourceConfigEntity(eventSourceConfigDto);
        int result = eventSourceConfigDomainService.updateByIdSelective(entity);
        //publishConfigChange
        publishConfigChange(entity.getId(), PublishParams.ActionType.DEL);
        return Result.wrapSuccess(result);
    }

    private EventSourceConfigEntity buildEventSourceConfigEntity(EventSourceConfigDto eventSourceConfigDto) {
        return EventSourceConfigEntity.builder()
                .id(eventSourceConfigDto.getId())
                .updateUser(eventSourceConfigDto.getUpdateUser())
                .isDelete(1)
                .build();
    }

    private boolean checkRule(List<Long> ruleConfigIdList) {
        if (!ruleConfigIdList.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean checkParm(EventSourceConfigDto eventSourceConfigDto) {
        if (eventSourceConfigDto == null || eventSourceConfigDto.getId() == null || eventSourceConfigDto.getId() <= 0) {
            return false;
        }
        return true;
    }


    private List<Long> useEventRuleConfigIdList(EventSourceConfigDto eventSourceConfigDto) {
        CheckRuleConfigEntity ruleConfigEntity = CheckRuleConfigEntity.builder()
                .eventId(eventSourceConfigDto.getId())
                .isDelete(0)
                .status(1)
                .build();

        List<CheckRuleConfigEntity> checkRuleConfigEntities =
                ruleConfigDomainService.selectCheckRuleConfigList(ruleConfigEntity);

        List<Long> ruleConfigIdList = new ArrayList<>();
        for (CheckRuleConfigEntity entity : checkRuleConfigEntities) {
            ruleConfigIdList.add(entity.getId());
        }
        return ruleConfigIdList;
    }

    @Override
    public Result<EventSourceConfigDto> info(Long eventSourceId) {
        //check Parm
        if (checkParm(eventSourceId)){
            return Result.wrapSuccess(null);
        }
        EventSourceConfigEntity entity = eventSourceConfigDomainService.getById(eventSourceId);
        //checkEntity
        EventSourceConfigDto dto = checkEntity(entity);
        logInfo("query eventSource by id:{}, result:{}", eventSourceId, dto);
        return Result.wrapSuccess(dto);
    }

    private EventSourceConfigDto checkEntity( EventSourceConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        return convertToEventSourceConfigDto(entity);
    }

    private boolean checkParm(Long eventSourceId) {
        if (null == eventSourceId && eventSourceId < 0) {
            return true;
        }
        return false;
    }

    private Result disableOrEnable(EventSourceConfigDto eventSourceConfigDto, int status) {
        if (eventSourceConfigDto == null || eventSourceConfigDto.getId() == null || eventSourceConfigDto.getId() <= 0) {
            return Result.wrapSuccess(0);
        } else {
            EventSourceConfigEntity entity = EventSourceConfigEntity.builder()
                    .id(eventSourceConfigDto.getId())
                    .updateUser(eventSourceConfigDto.getUpdateUser())
                    .status(status)
                    .build();
            int result = eventSourceConfigDomainService.updateByIdSelective(entity);
            return Result.wrapSuccess(result);
        }
    }

    @Override
    public Result<PageResultDto<EventSourceConfigDto>> page(EventSourceConfigDto eventSourceConfigDto) {
        EventSourceConfigEntity entity = EventSourceConfigEntitys.queryParamFromEventSrouceConfigDto(eventSourceConfigDto);
        //setPageInfo
        setPageInfo(eventSourceConfigDto, entity);
        //pageTotal
        Long pageTotal = eventSourceConfigDomainService.pageTotal(entity);
        //queryByPage
        List<EventSourceConfigEntity> pageData = new ArrayList<>();
        if (pageTotal != null && pageTotal >= 0) {
            pageData = eventSourceConfigDomainService.queryByPage(entity);
        }
        //buildEventSourceConfigDto
        PageResultDto<EventSourceConfigDto> pageResultDto = buildEventSourceConfigDto(pageTotal, pageData);
        logInfo("query by page, condition:{}, result:{}", eventSourceConfigDto, pageResultDto);
        return Result.wrapSuccess(pageResultDto);
    }

    private PageResultDto<EventSourceConfigDto> buildEventSourceConfigDto(Long pageTotal, List<EventSourceConfigEntity> pageData) {
        PageResultDto<EventSourceConfigDto> pageResultDto = PageResultDto.<EventSourceConfigDto>builder()
                .total(pageTotal)
                .dataList(buildList(pageData))
                .build();
        return pageResultDto;
    }

    private void setPageInfo(EventSourceConfigDto eventSourceConfigDto, EventSourceConfigEntity entity) {
        entity.setPageNo(eventSourceConfigDto.getPageNo());
        entity.setPageSize(eventSourceConfigDto.getPageSize());
    }

    private EventSourceConfigEntity convertFromEventSourceConfigDto(EventSourceConfigDto eventSourceConfigDto) {
        EventSourceConfigEntity entity = EventSourceConfigEntity.builder()
                .description(eventSourceConfigDto.getDescription())
                .sampleRatio(eventSourceConfigDto.getSampleRatio())
                .appCode("")
                .name(eventSourceConfigDto.getName())
                .type(eventSourceConfigDto.getType())
                .createUser(eventSourceConfigDto.getCreateUser())
                .updateUser(eventSourceConfigDto.getUpdateUser())
                .id(eventSourceConfigDto.getId())
                .build();
        if (eventSourceConfigDto.getType() == EventSourceTypeEnum.KAFKA.getType()) {
            entity.setDetailConf(JSON.toJSONString(eventSourceConfigDto.getKafkaConnParam()));
        }
        if (eventSourceConfigDto.getType() == EventSourceTypeEnum.ROCKETMQ.getType()) {
            entity.setDetailConf(JSON.toJSONString(eventSourceConfigDto.getRocketMqConnParam()));
        }

        // 保存延迟类型
        DelayTypeDefine delayTypeDefine = DelayTypeDefine.fromCode(eventSourceConfigDto.getDelayTypeCode());
        entity.setDelayTypeCode(delayTypeDefine.getCode());
        DelayEventResourceConfigDO delayEventResourceConfigDO = DelayEventResourceConfigDO.builder()
                .delayxMinValue(eventSourceConfigDto.getDelayxMinValue())
                .delayAtValue(eventSourceConfigDto.getDelayAtValue())
                .build();
        entity.setDelayTypeParam(JSON.toJSONString(delayEventResourceConfigDO));

        // 保存多路数据源模版
        EventMatchTemplateEntity matchTempleteEntity = EventMatchTemplateEntity.builder()
                .templateName(eventSourceConfigDto.getMatchTemplate())
                .eventSourceId(eventSourceConfigDto.getId())
                .saveToMatchDbFlag(eventSourceConfigDto.getSaveToMatchDbFlag())
                .templateKey(MapContextFormator.simplifyMatchTemplate(eventSourceConfigDto.getMatchTemplate()))
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
        if (null == matchTempleteEntity.getTemplateName()) {
            matchTempleteEntity.setTemplateName("");
        }
        if (null == matchTempleteEntity.getTemplateKey()) {
            matchTempleteEntity.setTemplateKey("");
        }
        if (null == matchTempleteEntity.getSaveToMatchDbFlag()) {
            matchTempleteEntity.setSaveToMatchDbFlag(0);
        }
        entity.setMatchTemplateEntity(matchTempleteEntity);
        return entity;
    }

    /**
     * 转化为dto.
     *
     * @param entity ""
     * @return ""
     */
    public static EventSourceConfigDto convertToEventSourceConfigDto(EventSourceConfigEntity entity) {
        EventSourceConfigDto configDto = EventSourceConfigDto.builder()
                .appCode(entity.getAppCode())
                .saveToMatchDbFlag(0)
                .createTime(entity.getCreateTime())
                .createUser(entity.getCreateUser())
                .description(entity.getDescription())
                .id(entity.getId())
                .isDelete(entity.getIsDelete())
                .name(entity.getName())
                .sampleRatio(entity.getSampleRatio())
                .status(entity.getStatus())
                .type(entity.getType())
                .updateTime(entity.getUpdateTime())
                .updateUser(entity.getUpdateUser())
                .version(entity.getVersion())
                .build();
        if (configDto.getType() == EventSourceTypeEnum.KAFKA.getType()) {
            try {
                KafkaConnParamDto dto = JSON.parseObject(entity.getDetailConf(), KafkaConnParamDto.class);
                configDto.setKafkaConnParam(dto);
            } catch (Exception e) {
                log.error("json 反序列化失败:{}", entity.getDetailConf());
            }
        }
        if (configDto.getType() == EventSourceTypeEnum.ROCKETMQ.getType()) {
            try {
                RocketMqConnParamDto dto = JSON.parseObject(entity.getDetailConf(), RocketMqConnParamDto.class);
                configDto.setRocketMqConnParam(dto);
            } catch (Exception e) {
                log.error("json 反序列化失败:{}", entity.getDetailConf());
            }
        }
        DelayTypeDefine delayTypeDefine = DelayTypeDefine.fromCode(entity.getDelayTypeCode());
        configDto.setDelayTypeCode(delayTypeDefine.getCode());
        configDto.setDelayTypeName(delayTypeDefine.getName());
        if (!StringUtils.isEmpty(entity.getDelayTypeParam())) {
            DelayEventResourceConfigDO delayEventResourceConfigDO = JSON.parseObject(entity.getDelayTypeParam(), DelayEventResourceConfigDO.class);
            if (null != delayEventResourceConfigDO) {
                configDto.setDelayAtValue(delayEventResourceConfigDO.getDelayAtValue());
                configDto.setDelayxMinValue(delayEventResourceConfigDO.getDelayxMinValue());
            }
        }
        // 还原事件匹配模版
        if (null != entity.getMatchTemplateEntity()) {
            if (null != entity.getMatchTemplateEntity().getSaveToMatchDbFlag()) {
                configDto.setSaveToMatchDbFlag(entity.getMatchTemplateEntity().getSaveToMatchDbFlag());
            }
            configDto.setMatchTemplate(entity.getMatchTemplateEntity().getTemplateName());
        }
        return configDto;
    }

    @Override
    public Result<List<EventSourceConfigDto>> queryByName(String name) {
        //check parm
        if (checkName(name)){
            return Result.wrapSuccess(new ArrayList<>());
        }
        //queryByName
        List<EventSourceConfigEntity> list = eventSourceConfigDomainService.queryByName(name);
        //build List
        return Result.wrapSuccess(buildList( list));
    }

    private boolean checkName(String name) {
        if (StringUtils.isBlank(name)) {
            return true;
        }
        return false;
    }

    private List<EventSourceConfigDto> buildList( List<EventSourceConfigEntity> list) {
        List<EventSourceConfigDto> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)){
            return dtoList;
        }

        for (EventSourceConfigEntity entity : list) {
            EventSourceConfigDto configDto = convertToEventSourceConfigDto(entity);
            dtoList.add(configDto);
        }
        return dtoList;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}
