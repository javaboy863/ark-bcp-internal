package com.ark.bcp.app.service.rules;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.ark.bcp.app.utils.CodeTypeDefineUtils;
import com.missfresh.risk.bcp.domain.engine.frame.ComponentTypeEnum;
import com.missfresh.risk.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.missfresh.risk.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.missfresh.risk.bcp.domain.exception.CheckParameterException;
import com.missfresh.risk.bcp.domain.repository.riskbcp.BcpCheckRuleAlertConfigRepository;
import com.missfresh.risk.bcp.domain.repository.riskbcp.BcpCheckRuleRepairConfigRepository;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.util.AsyncUtil;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.domain.vo.AlertChannelParams;
import com.missfresh.risk.bcp.domain.vo.RepairParams;
import com.missfresh.risk.bcp.dto.*;
import com.missfresh.risk.bcp.enums.AlertChannelDefine;
import com.missfresh.risk.bcp.enums.CodeTypeDefine;
import com.missfresh.risk.bcp.enums.LogicOperatorDefine;
import com.missfresh.risk.bcp.enums.RepairTypeDefine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
@Service
public class RuleConfigInnerService {
    private static Logger log = LoggerFactory.getLogger(RuleConfigInnerService.class);
    @Resource
    private RuleConfigDomainService ruleConfigDomainService;

    @Resource
    private ConditionConfigService conditionConfigService;

    @Resource
    private DynamicCodeConfigService dynamicCodeConfigService;

    @Resource
    private EventSourceConfigDomainService eventSourceConfigDomainService;


    /**
     * todo 这里结构不太清晰，app层不应关心数据库的读取，以及 Transactional 的实现
     */
    @Resource
    private BcpCheckRuleAlertConfigRepository alertConfigRepository;
    @Resource
    private BcpCheckRuleRepairConfigRepository repairConfigRepository;

    /**
     * 保存规则配置
     * @param ruleConfigDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "riskBcpTxManager")
    public Result<Long> saveRuleConfig(RuleConfigDto ruleConfigDto) {
        // 保存规则基础信息
        CheckRuleConfigEntity checkRuleConfigEntity = saveRuleConfigBaseRule(ruleConfigDto);
        // 保存规则条件
        saveRuleConfigConditons(ruleConfigDto, checkRuleConfigEntity);
        // 保存报警渠道配置
        saveRuleConfigAlertChannel(ruleConfigDto, checkRuleConfigEntity);
        // 保存修复渠道配置
        saveRuleConfigRepairConfig(ruleConfigDto, checkRuleConfigEntity);
        // 返回保存的id.
        return Result.wrapSuccess(checkRuleConfigEntity.getId());
    }

    /**
     * 通知保存规则配置
     * @param ruleId
     */
    public void notifySaveRuleConfig(final Long ruleId) {
        AsyncUtil.run(() -> {
            if (null == ruleId || 0 == ruleId) {
                return;
            }
            // 通知规则新增
            publishRuleConfigChange(ruleId, PublishParams.ActionType.ADD);

            List<ConditionConfigEntity> conditions = conditionConfigService.findConditionConfigByRuleId(ruleId);
            for (ConditionConfigEntity condition : conditions) {
                publishConditionConfigChange(condition.getId(), PublishParams.ActionType.ADD);
            }
        });
    }

    /**
     * 修改规则配置
     * @param ruleConfigDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "riskBcpTxManager")
    public Result updateRuleConfig(RuleConfigDto ruleConfigDto) {
        updateRuleConfigBaseRule(ruleConfigDto);
        // 更新条件信息
        updateRuleConfigScriptConditon(ruleConfigDto);
        // 更新脚本信息
        updateRuleConfigDynamicCode(ruleConfigDto);
        // 更新报警渠道信息
        updateRuleConfigAlertChannel(ruleConfigDto);
        // 更新修复信息
        updateRuleConfigRepairChannel(ruleConfigDto);
        return Result.wrapSuccess(true);
    }

    /**
     * 通知修改规则配置
     * @param ruleId
     */
    public void notifyUpdateRuleConfig(Long ruleId) {
        AsyncUtil.run(() -> {
            if (null == ruleId || 0 == ruleId) {
                return;
            }
            //发布规则配置
            publishRuleConfigChange(ruleId, PublishParams.ActionType.UPDATE);

            //根据规则id 查询条件配置
            List<ConditionConfigEntity> conditions = conditionConfigService.findConditionConfigByRuleId(ruleId);
            for (ConditionConfigEntity condition : conditions) {
                publishConditionConfigChange(condition.getId(), PublishParams.ActionType.UPDATE);
            }
        });
    }

    /**
     * 查询规则配置列表
     * @param queryDto
     * @return
     */
    public Result<PageResultDto<RuleConfigPageResultDto>> queryRuleConfigList(RuleConfigPageQueryDto queryDto) {

        //将入参转为查询条件
        CheckRuleConfigEntity condition = convertByRuleConfigPageQueryDto(queryDto);
        //查询条数
        Long total = ruleConfigDomainService.queryRuleConfigListPageTotal(condition);
        log.debug("query by : {}. total:{}", condition, total);
        //转为RuleConfigPageResultDto
        List<RuleConfigPageResultDto> data = convertByCheckRuleConfigEntity(total,condition);

        //组装返回值
        PageResultDto<RuleConfigPageResultDto> pageResultDto = resultProcess(total,data,queryDto);

        return Result.wrapSuccess(pageResultDto);
    }

    /**
     * 删除规则配置
     * @param ruleConfigDto
     * @return
     */
    public Result deleteRuleConfig(RuleConfigDto ruleConfigDto) {
        //入参转换
        CheckRuleConfigEntity checkRuleConfigEntity = convertByRuleConfigDto(ruleConfigDto);
        //修改操作
        ruleConfigDomainService.update(checkRuleConfigEntity);
        //发布规则配置
        publishRuleConfigChange(checkRuleConfigEntity.getId(), PublishParams.ActionType.DEL);

        return Result.wrapSuccess(true);
    }

    /**
     * 停用规则配置
     * @param ruleConfigDto
     * @return
     */
    public Result disableRuleConfig(RuleConfigDto ruleConfigDto) {
        //入参转化
        CheckRuleConfigEntity checkRuleConfigEntity = CheckRuleConfigEntity.builder()
                .id(ruleConfigDto.getId())
                .updatedBy(ruleConfigDto.getUpdatedBy())
                .status(0)
                .build();

        //修改操作
        ruleConfigDomainService.update(checkRuleConfigEntity);

        //发布规则配置
        publishRuleConfigChange(checkRuleConfigEntity.getId(), PublishParams.ActionType.UPDATE);

        return Result.wrapSuccess(true);
    }

    /**
     * 启用规则配置
     * @param ruleConfigDto
     * @return
     */
    public Result enableRuleConfig(RuleConfigDto ruleConfigDto) {
        //检查入参
        checkEnableRuleConfig(ruleConfigDto);
        //入参转换
        CheckRuleConfigEntity checkRuleConfigEntity = CheckRuleConfigEntity.builder().id(ruleConfigDto.getId())
                .updatedBy(ruleConfigDto.getUpdatedBy()).status(1).build();

        //修改操作
        ruleConfigDomainService.update(checkRuleConfigEntity);
        //发布规则配置
        publishRuleConfigChange(checkRuleConfigEntity.getId(), PublishParams.ActionType.UPDATE);

        return Result.wrapSuccess(true);
    }

    /**
     * 规则配置详情
     * @param ruleId
     * @return
     */
    public Result<RuleConfigInfoDto> ruleConfigInfo(Long ruleId) {
        if (ruleId == null || ruleId <= 0) {
            return Result.wrapSuccess(null);
        }
        RuleConfigInfoDto ruleConfigInfoDto = new RuleConfigInfoDto();
        // 填充基本信息
        ruleConfigInfoAttachBaseRule(ruleId, ruleConfigInfoDto);
        // 填充条件信息
        ruleConfigInfoAttachConditions(ruleId, ruleConfigInfoDto);
        // 填充报警信息
        ruleConfigInfoAttachAlertChannel(ruleId, ruleConfigInfoDto);
        // 填充修复信息
        ruleConfigInfoAttachRepairDetail(ruleId, ruleConfigInfoDto);
        log.info("ruleId:{}, ruleConfig:{}", ruleId, ruleConfigInfoDto);
        return Result.wrapSuccess(ruleConfigInfoDto);
    }

    /**
     * 查询规则配置列表
     * @param ruleConfigDto
     * @return
     */
    public Result<List<RuleConfigPageResultDto>> searchRuleConfigList(RuleConfigDto ruleConfigDto) {
        //入参检查
        checkSearchRuleConfigList(ruleConfigDto);
        //查询条件组装
        CheckRuleConfigEntity entity = searchCheckRuleConfigByName(ruleConfigDto);
        //查询列表
        List<CheckRuleConfigEntity> ruleConfigEntities = ruleConfigDomainService.searchCheckRuleConfigList(entity);
        //返回实体组装
        List<RuleConfigPageResultDto> results = searchRuleConfigListResHandle(ruleConfigEntities);

        return ResultUtils.wrapSuccess(results);
    }

    /**
     * mockTest
     * @param ruleConfigDto
     * @return
     */
    public Result<List<MokeResultDto>> mockTest(RuleConfigDto ruleConfigDto) {

        //将mock data 组装到map context,k:Constant.EVENET_ID v mock data
        fieldSetterReaderSetFeild(ruleConfigDto.getMockData());

        //处理并返回
        return ResultUtils.wrapSuccess(mockTestHandle(ruleConfigDto));
    }

    /**
     * 检查是否可以启用
     * @param ruleConfigDto
     */
    private void checkEnableRuleConfig(RuleConfigDto ruleConfigDto){
        //根据事件id 查询
        EventSourceConfigEntity eventSourceConfigEntity = eventSourceConfigDomainService.getById(ruleConfigDto.getEventId());

        if (!eventSourceConfigEntity.isEnable()) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(), "关联的事件Id:[" + ruleConfigDto.getEventId() + "]被删除或不可用,不能启用!");
        }

    }

    /**
     * 填充修复信息
     * @param ruleId
     * @param ruleConfigInfoDto
     */
    private void ruleConfigInfoAttachRepairDetail(Long ruleId, RuleConfigInfoDto ruleConfigInfoDto) {
        BcpCheckRuleRepairEntity query = BcpCheckRuleRepairEntity.builder().ruleId(ruleId).build();
        log.info("查找修复信息:{}", ruleId);
        List<BcpCheckRuleRepairEntity> entities = repairConfigRepository.selectBcpCheckRuleRepairConfigList(query);
        log.info("查找修复信息,结果:{}", entities);
        RepairTypeDefine repairTypeDefine = null;
        if (!CollectionUtils.isEmpty(entities)) {
            BcpCheckRuleRepairEntity entity = entities.get(0);
            repairTypeDefine = RepairTypeDefine.fromCode(entity.getRepairType());
            if (null == repairTypeDefine) {
                repairTypeDefine = RepairTypeDefine.NO_REPAIR;
            }
            RepairParams repairParams = JSON.parseObject(entity.getRepairConfigJson(), RepairParams.class);
            RepairConfigDto repairConfigDto = RepairConfigDto.builder()
                    .repairTypeCode(repairTypeDefine.getCode())
                    .build();
            if (null != repairParams) {
                repairConfigDto.setHttpCallbackUrl(repairParams.getCallbackUrl());
            }
            ruleConfigInfoDto.setRepairConfigDto(repairConfigDto);
        } else {
            RepairConfigDto repairConfigDto = RepairConfigDto.builder()
                    .repairTypeCode(RepairTypeDefine.NO_REPAIR.getCode()).build();
            ruleConfigInfoDto.setRepairConfigDto(repairConfigDto);
        }
    }

    /**
     * 填充报警信息
     * @param ruleId
     * @param ruleConfigInfoDto
     */
    private void ruleConfigInfoAttachAlertChannel(Long ruleId, RuleConfigInfoDto ruleConfigInfoDto) {
        BcpCheckRuleAlertEntity query = BcpCheckRuleAlertEntity.builder().ruleId(ruleId).build();
        log.info("查找报警规则配置:{}", ruleId);
        List<BcpCheckRuleAlertEntity> entities = alertConfigRepository.selectBcpCheckRuleAlertConfigList(query);
        log.info("查找报警规则配置,结果:{}", entities);
        AlertChannelDefine alertChannelDefine = null;
        if (!CollectionUtils.isEmpty(entities)) {
            BcpCheckRuleAlertEntity entity = entities.get(0);
            alertChannelDefine = AlertChannelDefine.fromCode(entity.getAlertType());
            if (null == alertChannelDefine) {
                alertChannelDefine = AlertChannelDefine.ALERT_LARK_GROUP;
            }
            AlertConfigDto alertConfigDto = AlertConfigDto.builder()
                    .alertChannelCode(alertChannelDefine.getCode())
                    .alertTextFormat(entity.getAlertTextFormat())
                    .build();
            AlertChannelParams params = JSON.parseObject(entity.getAlertConfigJson(), AlertChannelParams.class);
            if (null != params) {
                alertConfigDto.setAlertGroupChatUrl(params.getAlertUrl());
            }
            ruleConfigInfoDto.setAlertConfigDto(alertConfigDto);
        } else {
            AlertConfigDto alertConfigDto = AlertConfigDto.builder().alertChannelCode(AlertChannelDefine.ALERT_LARK_GROUP.getCode()).build();
            ruleConfigInfoDto.setAlertConfigDto(alertConfigDto);
        }

    }

    private void ruleConfigInfoAttachEventDetail(Long eventid, RuleConfigInfoDto ruleConfigInfoDto) {
        EventSourceConfigEntity eventSourceConfigEntity = eventSourceConfigDomainService.getById(eventid);
        ruleConfigInfoDto.setEventName(eventSourceConfigEntity.getName());
        ruleConfigInfoDto.setEventDetailConf(eventSourceConfigEntity.getDetailConf());
        ruleConfigInfoDto.setEventStatus(eventSourceConfigEntity.getStatus());
        ruleConfigInfoDto.setEventType(eventSourceConfigEntity.getType());
        ruleConfigInfoDto.setEventAppCode(eventSourceConfigEntity.getAppCode());
    }

    private void ruleConfigInfoAttachCodeConditons(
            List<ConditionConfigEntity> conditionConfigEntitys,
            RuleConfigInfoDto ruleConfigInfoDto) {
        List<RuleCodeDto> ruleCodeDtoList = new ArrayList<>();
        int conditionType = 0;
        for (ConditionConfigEntity entity : conditionConfigEntitys) {
            RuleCodeDto ruleCodeDto = RuleCodeDto.builder().build();
            final CodeTypeDefine codeTypeDefine = CodeTypeDefineUtils.fromConditionType(entity.getType());
            if (null != codeTypeDefine) {
                BaseDynamicScriptParameter parameter = BaseDynamicScriptParameter.newInstance(entity.getParams());
                if (null != parameter) {
                    DynamicCodeConfigEntity codeConfigEntity = dynamicCodeConfigService.queryCodeListById(Long.valueOf(parameter.getScriptId()));
                    if (null != codeConfigEntity) {
                        ruleCodeDto.setScriptContent(codeConfigEntity.getScriptContent());
                        ruleCodeDto.setId(codeConfigEntity.getId());
                        ruleCodeDto.setCodeName(codeConfigEntity.getName());
                        ruleCodeDto.setConditionId(entity.getId());
                        ruleCodeDto.setCodeType(codeTypeDefine.getCode());
                        ruleCodeDtoList.add(ruleCodeDto);
                    }
                }
            }

            if (entity.getType() == ConditionTypeEnum.CONDITION_SET.getCode()) {
                ConditionGroupParameter parameter = ConditionGroupParameter.newInstance(entity.getParams());
                if (null != parameter) {
                    conditionType = parameter.getLogicOperator();
                }
            }
        }
        ruleConfigInfoDto.setCodeList(ruleCodeDtoList);
        ruleConfigInfoDto.setConditionType(conditionType);
    }

    /**
     * 填充条件信息
     * @param ruleId
     * @param ruleConfigInfoDto
     */
    private void ruleConfigInfoAttachConditions(Long ruleId, RuleConfigInfoDto ruleConfigInfoDto) {
        List<ConditionConfigEntity> conditionConfigEntitys = conditionConfigService.findConditionConfigByRuleId(ruleId);
        ruleConfigInfoAttachCodeConditons(conditionConfigEntitys, ruleConfigInfoDto);
    }

    /**
     * 填充基本信息
     * @param ruleId
     * @param ruleConfigInfoDto
     */
    private void ruleConfigInfoAttachBaseRule(Long ruleId, RuleConfigInfoDto ruleConfigInfoDto) {
        CheckRuleConfigEntity checkRuleConfigEntity = ruleConfigDomainService.selectCheckRuleConfigById(ruleId);
        ruleConfigInfoDto.setRuleName(checkRuleConfigEntity.getRuleName());
        ruleConfigInfoDto.setId(checkRuleConfigEntity.getId());
        ruleConfigInfoDto.setAppCode(checkRuleConfigEntity.getAppCode());
        ruleConfigInfoDto.setCreatedBy(checkRuleConfigEntity.getCreatedBy());
        ruleConfigInfoDto.setCreatedTime(checkRuleConfigEntity.getCreatedTime());
        ruleConfigInfoDto.setDescription(checkRuleConfigEntity.getDescription());
        ruleConfigInfoDto.setMockData(checkRuleConfigEntity.getMockData());
        ruleConfigInfoDto.setIsDelete(checkRuleConfigEntity.getIsDelete());
        ruleConfigInfoDto.setEventId(checkRuleConfigEntity.getEventId());
        ruleConfigInfoDto.setStatus(checkRuleConfigEntity.getStatus());
        ruleConfigInfoDto.setUpdatedBy(checkRuleConfigEntity.getUpdatedBy());
        ruleConfigInfoDto.setUpdatedTime(checkRuleConfigEntity.getUpdatedTime());
        ruleConfigInfoDto.setVersion(checkRuleConfigEntity.getVersion());
        // 扩展获取事件详情
        ruleConfigInfoAttachEventDetail(checkRuleConfigEntity.getEventId(), ruleConfigInfoDto);
    }

    /**
     * 返回实体组装
     * @param ruleConfigEntities
     * @return
     */
    private List<RuleConfigPageResultDto> searchRuleConfigListResHandle(List<CheckRuleConfigEntity> ruleConfigEntities){

        if (CollectionUtils.isEmpty(ruleConfigEntities)) {
            return Lists.newArrayList();
        }

        List<RuleConfigPageResultDto> results = ruleConfigEntities.stream().map(
                new Function<CheckRuleConfigEntity, RuleConfigPageResultDto>() {
                    @Override
                    public RuleConfigPageResultDto apply(CheckRuleConfigEntity entity) {
                        RuleConfigPageResultDto result = RuleConfigPageResultDto.builder()
                                .appCode(entity.getAppCode())
                                .createdBy(entity.getCreatedBy())
                                .createdTime(entity.getCreatedTime())
                                .description(entity.getDescription())
                                .eventId(entity.getEventId())
                                .id(entity.getId())
                                .isDelete(entity.getIsDelete())
                                .ruleName(entity.getRuleName())
                                .status(entity.getStatus())
                                .updatedBy(entity.getUpdatedBy())
                                .updatedTime(entity.getUpdatedTime())
                                .version(entity.getVersion())
                                .build();
                        return result;
                    }
                }
        ).collect(Collectors.toList());

        return results;
    }


    /**
     *
     * @param ruleConfigDto
     * @return
     */
    private CheckRuleConfigEntity searchCheckRuleConfigByName(RuleConfigDto ruleConfigDto){

        CheckRuleConfigEntity entity = new CheckRuleConfigEntity();
        entity.setRuleName(ruleConfigDto.getRuleName());
        entity.setIsDelete(0);

        return entity;
    }

    /**
     * 入参检查
     * @param ruleConfigDto
     */
    private void checkSearchRuleConfigList(RuleConfigDto ruleConfigDto){
        if (null == ruleConfigDto || StringUtils.isEmpty(ruleConfigDto.getRuleName())) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(), "规则名不能为空");
        }
    }


    /**
     * 转换入参
     * @param ruleConfigDto
     * @return
     */
    private CheckRuleConfigEntity convertByRuleConfigDto(RuleConfigDto ruleConfigDto){
        return CheckRuleConfigEntity.builder()
                .id(ruleConfigDto.getId())
                .updatedBy(ruleConfigDto.getUpdatedBy())
                .isDelete(1)
                .build();
    }

    /**
     *组装context
     * @param value
     */
    private void fieldSetterReaderSetFeild(String value){
        Map<String, String> context = Maps.newHashMap();
        FieldSetterReader.Setter.setFeild(context, Constant.EVENET_ID, value);
    }

    /**
     * mockTest 处理流程
     * @param ruleConfigDto
     * @return
     */
    private List<MokeResultDto> mockTestHandle(RuleConfigDto ruleConfigDto){
        List<MokeResultDto> resultList = Lists.newArrayList();

        for (RuleCodeDto ruleCodeDto : ruleConfigDto.getCodeList()) {
            ConditionConfigEntity conditionConfigEntity = new ConditionConfigEntity();

            final ConditionTypeEnum conditionTypeEnum = CodeTypeDefineUtils.toConditionType(ruleCodeDto.getCodeType());
            if (conditionTypeEnum != null) {
                conditionConfigEntity.setType(conditionTypeEnum.getCode());
            }

            BaseDynamicScriptParameter parameter = new BaseDynamicScriptParameter();
            parameter.setRawScriptSource(ruleCodeDto.getScriptContent());
            conditionConfigEntity.setParamsObject(parameter);
            conditionConfigEntity.setParams(JSON.toJSONString(parameter));
            ContextWrap contextWrap = new ContextWrap();
            FieldSetterReader.Setter.setRuntimeField(contextWrap, Constant.RT_RAW_SCRIPT_PARAM, JSON.parseObject(ruleConfigDto.getMockData()));

            AbstractConditionBO conditionAbstractBO = ConditionFactory.createConditionBo(conditionConfigEntity, null);
            ExecuteResult executeResult = conditionAbstractBO.conditionInvoke(contextWrap);

            MokeResultDto mokeResultDto = new MokeResultDto();
            mokeResultDto.setAvailable(executeResult.getAvailable());
            if (!executeResult.getAvailable()) {
                mokeResultDto.setPromopt(Joiner.on("\n").join(executeResult.getPromotMsgs()));
            } else {
                mokeResultDto.setPromopt((null != executeResult.getReuslt() && executeResult.getReuslt()) ? "规则命中" : "规则未命中");
            }
            resultList.add(mokeResultDto);
        }

        return resultList;
    }

    /**
     * 将ruleConfigPageQueryDto 转为 CheckRuleConfigEntity
     * @param queryDto
     * @return
     */
    private CheckRuleConfigEntity convertByRuleConfigPageQueryDto(RuleConfigPageQueryDto queryDto){
        CheckRuleConfigEntity condition = CheckRuleConfigEntity.builder()
                .appCode(queryDto.getAppCode())
                .ruleName(queryDto.getRuleName())
                .id(queryDto.getId())
                .eventId(queryDto.getEventId())
                .createdBy(queryDto.getCreatedBy())
                .status(queryDto.getStatus())
                .isDelete(0)
                .build();
        condition.setPageNo(queryDto.getPageNo());
        condition.setPageSize(queryDto.getPageSize());

        return condition;
    }

    /**
     *转化为RuleConfigPageResultDto list
     * @param total
     * @param condition
     * @return
     */
    private List<RuleConfigPageResultDto> convertByCheckRuleConfigEntity(Long total,CheckRuleConfigEntity condition){
        List<RuleConfigPageResultDto> data = new ArrayList<>();
        List<CheckRuleConfigEntity> checkRuleConfigEntities = Collections.EMPTY_LIST;
        if (total != null && total > 0) {
            checkRuleConfigEntities = ruleConfigDomainService.queryByPage(condition);
        }

        log.debug("query by:{}, list:{}", condition, checkRuleConfigEntities);
        for (CheckRuleConfigEntity entity : checkRuleConfigEntities) {
            RuleConfigPageResultDto result = RuleConfigPageResultDto.builder()
                    .appCode(entity.getAppCode())
                    .createdBy(entity.getCreatedBy())
                    .createdTime(entity.getCreatedTime())
                    .description(entity.getDescription())
                    .mockData(entity.getMockData())
                    .eventId(entity.getEventId())
                    .id(entity.getId())
                    .isDelete(entity.getIsDelete())
                    .ruleName(entity.getRuleName())
                    .status(entity.getStatus())
                    .updatedBy(entity.getUpdatedBy())
                    .updatedTime(entity.getUpdatedTime())
                    .version(entity.getVersion())
                    .build();
            data.add(result);
        }

       return data;
    }

    /**
     * 返回结果组装
     * @param total
     * @param data
     * @param queryDto
     * @return
     */
    private PageResultDto<RuleConfigPageResultDto> resultProcess(Long total,List<RuleConfigPageResultDto> data,RuleConfigPageQueryDto queryDto){

        PageResultDto<RuleConfigPageResultDto> pageResultDto = PageResultDto.<RuleConfigPageResultDto>builder()
                .dataList(data)
                .total(total)
                .build();
        log.debug("query by : {}, result:{}", queryDto, pageResultDto);
        return pageResultDto;
    }

    /**
     * 检查规则配置查询条件
     * @param ruleConfigDto
     * @return
     */
    private CheckRuleConfigEntity checkRuleConfigEntityFromRuleConfigDto(RuleConfigDto ruleConfigDto) {
        return CheckRuleConfigEntity.builder()
                .createdBy(ruleConfigDto.getCreatedBy())
                .updatedBy(ruleConfigDto.getUpdatedBy())
                .ruleName(ruleConfigDto.getRuleName())
                .appCode(ruleConfigDto.getAppCode())
                .description(ruleConfigDto.getDescription())
                .eventId(ruleConfigDto.getEventId())
                .mockData(ruleConfigDto.getMockData())
                .id(ruleConfigDto.getId())
                .build();
    }

    /**
     * 广播规则配置
     * @param ruleid
     * @param actionType
     */
    private void publishRuleConfigChange(Long ruleid, PublishParams.ActionType actionType) {
        PublishParams publishParams = new PublishParams();
        publishParams.setComponentTypeEnum(ComponentTypeEnum.RULE);
        publishParams.setTypeId(String.valueOf(ruleid));
        publishParams.setAction(actionType);
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        if (null != configChangeNotifier) {
            configChangeNotifier.doPublish(Constant.BROADCAST_ID_EVENT, JSON.toJSONString(publishParams));
        } else {
            log.info("获取广播失败");
        }
    }

    /**
     * 广播条件
     * @param conditionid
     * @param actionType
     */
    private void publishConditionConfigChange(Long conditionid, PublishParams.ActionType actionType) {
        PublishParams publishParams = new PublishParams();
        publishParams.setComponentTypeEnum(ComponentTypeEnum.CONDITION);
        publishParams.setTypeId(String.valueOf(conditionid));
        publishParams.setAction(actionType);
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        if (null != configChangeNotifier) {
            configChangeNotifier.doPublish(Constant.BROADCAST_ID_EVENT, JSON.toJSONString(publishParams));
        } else {
            log.info("获取广播失败");
        }
    }

    /**
     * 保存规则配置
     * @param ruleConfigDto
     * @return
     */
    private CheckRuleConfigEntity saveRuleConfigBaseRule(final RuleConfigDto ruleConfigDto) {
        CheckRuleConfigEntity checkRuleConfigEntity = checkRuleConfigEntityFromRuleConfigDto(ruleConfigDto);
        ruleConfigDomainService.addRuleConfig(checkRuleConfigEntity);
        return checkRuleConfigEntity;
    }

    /**
     * 保存规则配置条件
     * @param ruleConfigDto
     * @param checkRuleConfigEntity
     */
    private void saveRuleConfigConditons(final RuleConfigDto ruleConfigDto,
                                         final CheckRuleConfigEntity checkRuleConfigEntity) {
        // 增加一个根条件组
        ConditionGroupParameter parameter = new ConditionGroupParameter();
        //com.missfresh.risk.bcp.domain.engine.frame.decision.execute.LogicOperatorEnum
        parameter.setLogicOperator(ruleConfigDto.getConditionType());
        ConditionConfigEntity conditonGroup = ConditionConfigEntity.builder()
                .createdBy(ruleConfigDto.getCreatedBy())
                .parentId(0L)
                .ruleId(checkRuleConfigEntity.getId())
                .type(ConditionTypeEnum.CONDITION_SET.getCode())
                .params(JSON.toJSONString(parameter))
                .build();
        conditionConfigService.addCondition(conditonGroup);
        // 增加详细条件
        saveRuleConfigScriptConditon(ruleConfigDto, checkRuleConfigEntity, conditonGroup);
    }

    /**
     * 保存规则配置脚本条件
     * @param ruleConfigDto
     * @param checkRuleConfigEntity
     * @param conditonGroup
     */
    private void saveRuleConfigScriptConditon(final RuleConfigDto ruleConfigDto,
                                              final CheckRuleConfigEntity checkRuleConfigEntity,
                                              final ConditionConfigEntity conditonGroup
    ) {
        // 再每个脚本增加一个条件
        for (RuleCodeDto code : ruleConfigDto.getCodeList()) {
            DynamicCodeConfigEntity dynamicCodeConfigEntity = DynamicCodeConfigEntity.builder()
                    .conditionId(0L)
                    .createdBy(ruleConfigDto.getCreatedBy())
                    .scriptContent(code.getScriptContent())
                    .name(code.getCodeName())
                    .type(code.getCodeType())
                    .build();
            dynamicCodeConfigService.addCode(dynamicCodeConfigEntity);

            ConditionConfigEntity condition = ConditionConfigEntity.builder()
                    .createdBy(ruleConfigDto.getCreatedBy())
                    .parentId(conditonGroup.getId())
                    .ruleId(checkRuleConfigEntity.getId())
                    .build();
            final ConditionTypeEnum conditionTypeEnum = CodeTypeDefineUtils.toConditionType(code.getCodeType());
            if (null != conditionTypeEnum) {
                BaseDynamicScriptParameter parameter1 = new BaseDynamicScriptParameter();
                parameter1.setScriptId(String.valueOf(dynamicCodeConfigEntity.getId()));
                condition.setParams(JSON.toJSONString(parameter1));
                condition.setType(conditionTypeEnum.getCode());
            } else {
                continue;
            }

            conditionConfigService.addCondition(condition);
        }
    }

    /**
     * 保存规则配置通知渠道
     * @param ruleConfigDto
     * @param checkRuleConfigEntity
     */
    private void saveRuleConfigAlertChannel(final RuleConfigDto ruleConfigDto,
                                            final CheckRuleConfigEntity checkRuleConfigEntity) {
        AlertChannelDefine alertChannelDefine = null;
        AlertChannelParams params = AlertChannelParams.builder().build();

        AlertConfigDto alertConfigDto = ruleConfigDto.getAlertConfigDto();
        String alertFormat = null;
        if (null != alertConfigDto) {
            alertChannelDefine = AlertChannelDefine.fromCode(ruleConfigDto.getAlertConfigDto().getAlertChannelCode());
            params.setAlertUrl(alertConfigDto.getAlertGroupChatUrl());
            alertFormat = alertConfigDto.getAlertTextFormat();
        }
        if (null == alertChannelDefine) {
            alertChannelDefine = AlertChannelDefine.ALERT_LARK_GROUP;
        }

        BcpCheckRuleAlertEntity entity = BcpCheckRuleAlertEntity.builder()
                .zipped(0)
                .ruleId(checkRuleConfigEntity.getId())
                .alertType(alertChannelDefine.getCode())
                .alertTextFormat(alertFormat)
                .alertConfigJson(JSON.toJSONString(params))
                .build();
        log.info("保存报警信息:{}", entity.toString());
        alertConfigRepository.insertBcpCheckRuleAlertConfig(entity);
        log.info("保存报警信息,结果:{}", entity.getId());
    }

    /**
     * 保存规则配置修复配置
     * @param ruleConfigDto
     * @param checkRuleConfigEntity
     */
    private void saveRuleConfigRepairConfig(final RuleConfigDto ruleConfigDto,
                                            final CheckRuleConfigEntity checkRuleConfigEntity) {
        RepairTypeDefine repairTypeDefine = null;
        RepairConfigDto repairConfigDto = ruleConfigDto.getRepairConfigDto();
        RepairParams params = RepairParams.builder().build();
        if (null != repairConfigDto) {
            repairTypeDefine = RepairTypeDefine.fromCode(repairConfigDto.getRepairTypeCode());
            params.setCallbackUrl(repairConfigDto.getHttpCallbackUrl());
        }
        if (null == repairTypeDefine) {
            repairTypeDefine = RepairTypeDefine.NO_REPAIR;
        }
        BcpCheckRuleRepairEntity entity = BcpCheckRuleRepairEntity.builder()
                .zipped(0)
                .repairType(repairTypeDefine.getCode())
                .repairConfigJson(JSON.toJSONString(params))
                .ruleId(checkRuleConfigEntity.getId())
                .build();
        log.info("保存修复信息:{}", entity.toString());
        repairConfigRepository.insertBcpCheckRuleRepairConfig(entity);
        log.info("保存修复信息,结果:{}", entity.getId());
    }

    private void updateRuleConfigBaseRule(final RuleConfigDto ruleConfigDto) {
        CheckRuleConfigEntity checkRuleConfigEntity = checkRuleConfigEntityFromRuleConfigDto(ruleConfigDto);
        ruleConfigDomainService.update(checkRuleConfigEntity);
    }

    private void updateRuleConfigScriptConditon(final RuleConfigDto ruleConfigDto) {
        // 更新根条件组
        ConditionGroupParameter parameter = new ConditionGroupParameter();
        //com.missfresh.risk.bcp.domain.engine.frame.decision.execute.LogicOperatorEnum
        parameter.setLogicOperator(ruleConfigDto.getConditionType());
        ConditionConfigEntity conditionConfig = conditionConfigService.findConditionConfigByRuleIdAndType(ruleConfigDto.getId(), ConditionTypeEnum.CONDITION_SET.getCode());
        conditionConfig.setParams(JSON.toJSONString(parameter));
        conditionConfigService.update(conditionConfig);
        if (CollectionUtils.isEmpty(ruleConfigDto.getCodeList())) {
            return;
        }
        for (RuleCodeDto ruleCodeDto : ruleConfigDto.getCodeList()) {
            ConditionConfigEntity condition = ConditionConfigEntity.builder()
                    .id(ruleCodeDto.getConditionId())
                    .type(ruleConfigDto.getConditionType())
                    .updatedBy(ruleConfigDto.getUpdatedBy())
                    .build();

            final ConditionTypeEnum conditionTypeEnum = CodeTypeDefineUtils.toConditionType(ruleCodeDto.getCodeType());
            if (null != conditionTypeEnum) {
                BaseDynamicScriptParameter parameter1 = new BaseDynamicScriptParameter();
                parameter1.setScriptId(String.valueOf(ruleCodeDto.getId()));
                condition.setParams(JSON.toJSONString(parameter1));
                condition.setType(conditionTypeEnum.getCode());
            } else {
                continue;
            }

            log.info("更新脚本条件:{}", JSON.toJSONString(condition));
            int effectRow = conditionConfigService.update(condition);
            log.info("更新脚本条件结果:{}", effectRow);
        }
    }

    private void updateRuleConfigDynamicCode(final RuleConfigDto ruleConfigDto) {
        List<DynamicCodeConfigEntity> inputEntities = convertDynamicCodeConfigEntities(ruleConfigDto);

        List<DynamicCodeConfigEntity> newEntities = newCodeConfigEntities(inputEntities);

        List<DynamicCodeConfigEntity> codeConfigEntities = dynamicCodeConfigService.queryCodeListByRuleId(ruleConfigDto.getId());
        log.debug("find codeConfig:{}", codeConfigEntities);

        List<DynamicCodeConfigEntity> updateEntities = updateDynamicCodeConfigEntities(inputEntities, codeConfigEntities);

        List<DynamicCodeConfigEntity> deleteEntities = deleteDynamicCodeConfigEntities(codeConfigEntities);

        //add
        for (DynamicCodeConfigEntity entity : newEntities) {
            log.info("add dynamic code:{}", JSON.toJSONString(entity));
            int effectRow = dynamicCodeConfigService.addCode(entity);
            log.info("add dynamic code result:{}", effectRow);
        }

        //update
        for (DynamicCodeConfigEntity entity : updateEntities) {
            log.info("add dynamic code:{}", JSON.toJSONString(entity));
            int effectRow = dynamicCodeConfigService.update(entity);
            log.info("add dynamic code result:{}", effectRow);
        }

        //delete
        for (DynamicCodeConfigEntity entity : deleteEntities) {
            log.info("del dynamic code:{}", JSON.toJSONString(entity));
            int effectRow = dynamicCodeConfigService.update(entity);
            log.info("add dynamic code result:{}", effectRow);
        }
    }

    private void updateRuleConfigAlertChannel(final RuleConfigDto ruleConfigDto) {
        AlertChannelDefine alertChannelDefine = null;
        AlertChannelParams params = AlertChannelParams.builder().build();

        AlertConfigDto alertConfigDto = ruleConfigDto.getAlertConfigDto();
        String textFormat = null;
        if (null != alertConfigDto) {
            alertChannelDefine = AlertChannelDefine.fromCode(ruleConfigDto.getAlertConfigDto().getAlertChannelCode());
            params.setAlertUrl(alertConfigDto.getAlertGroupChatUrl());
            textFormat = alertConfigDto.getAlertTextFormat();
        }
        if (null == alertChannelDefine) {
            alertChannelDefine = AlertChannelDefine.ALERT_LARK_GROUP;
        }

        BcpCheckRuleAlertEntity entity = BcpCheckRuleAlertEntity.builder()
                .zipped(0)
                .ruleId(ruleConfigDto.getId())
                .alertType(alertChannelDefine.getCode())
                .alertConfigJson(JSON.toJSONString(params))
                .alertTextFormat(textFormat)
                .build();
        log.info("更新报警信息:{}", entity.toString());
        alertConfigRepository.insertBcpCheckRuleAlertConfig(entity);
        log.info("更新报警信息,结果:{}", entity.getId());
    }

    private void updateRuleConfigRepairChannel(final RuleConfigDto ruleConfigDto) {
        RepairTypeDefine repairTypeDefine = null;
        RepairConfigDto repairConfigDto = ruleConfigDto.getRepairConfigDto();
        RepairParams params = RepairParams.builder().build();
        if (null != repairConfigDto) {
            repairTypeDefine = RepairTypeDefine.fromCode(repairConfigDto.getRepairTypeCode());
            params.setCallbackUrl(repairConfigDto.getHttpCallbackUrl());
        }
        if (null == repairTypeDefine) {
            repairTypeDefine = RepairTypeDefine.NO_REPAIR;
        }
        BcpCheckRuleRepairEntity entity = BcpCheckRuleRepairEntity.builder()
                .zipped(0)
                .repairType(repairTypeDefine.getCode())
                .repairConfigJson(JSON.toJSONString(params))
                .ruleId(ruleConfigDto.getId())
                .build();
        log.info("更新修复信息:{}", entity.toString());
        repairConfigRepository.insertBcpCheckRuleRepairConfig(entity);
        log.info("更新修复信息,结果:{}", entity.getId());
    }


    private List<DynamicCodeConfigEntity> deleteDynamicCodeConfigEntities(List<DynamicCodeConfigEntity> codeConfigEntities) {
        List<DynamicCodeConfigEntity> deleteEntities = new ArrayList<>();
        for (DynamicCodeConfigEntity entity : codeConfigEntities) {
            DynamicCodeConfigEntity deleteEntity = DynamicCodeConfigEntity.builder()
                    .isDelete(1)
                    .id(entity.getId())
                    .build();
            deleteEntities.add(deleteEntity);
        }
        log.debug("delete codeConfig:{}", deleteEntities);
        return deleteEntities;
    }

    private List<DynamicCodeConfigEntity> updateDynamicCodeConfigEntities(List<DynamicCodeConfigEntity> inputEntities, List<DynamicCodeConfigEntity> codeConfigEntities) {
        List<DynamicCodeConfigEntity> updateEntities = new ArrayList<>();
        for (DynamicCodeConfigEntity entity : inputEntities) {
            if (codeConfigEntities.contains(entity)) {
                updateEntities.add(entity);
            }
        }
        codeConfigEntities.removeAll(updateEntities);
        log.debug("update codeConfig:{}", updateEntities);
        return updateEntities;
    }

    private List<DynamicCodeConfigEntity> newCodeConfigEntities(List<DynamicCodeConfigEntity> inputEntities) {
        List<DynamicCodeConfigEntity> newEntities = new ArrayList<>();
        for (DynamicCodeConfigEntity entity : inputEntities) {
            if (entity.getId() == null) {
                newEntities.add(entity);
            }
        }
        inputEntities.removeAll(newEntities);
        log.debug("new codeConfig:{}", newEntities);
        return newEntities;
    }

    private List<DynamicCodeConfigEntity> convertDynamicCodeConfigEntities(RuleConfigDto ruleConfigDto) {
        List<DynamicCodeConfigEntity> inputEntities = new ArrayList<>();
        for (RuleCodeDto ruleCodeDto : ruleConfigDto.getCodeList()) {
            DynamicCodeConfigEntity entity = DynamicCodeConfigEntity.builder()
                    .type(ruleCodeDto.getCodeType())
                    .scriptContent(ruleCodeDto.getScriptContent())
                    .name(ruleCodeDto.getCodeName())
                    .updatedBy(ruleConfigDto.getUpdatedBy())
                    .conditionId(ruleCodeDto.getConditionId())
                    .id(ruleCodeDto.getId())
                    .build();
            inputEntities.add(entity);
        }
        log.debug("input entities:{}", inputEntities);
        return inputEntities;
    }


}
