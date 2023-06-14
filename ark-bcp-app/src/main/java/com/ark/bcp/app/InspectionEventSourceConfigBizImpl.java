package com.ark.bcp.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.app.utils.CronFieldEntitys;
import com.ark.bcp.app.utils.EventSourceConfigEntitys;
import com.ark.bcp.app.utils.InspectionEventSourceEntitys;
import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.InspectionEventSourceConfigService;
import com.missfresh.risk.bcp.domain.constant.EventSourceStatusEnum;
import com.missfresh.risk.bcp.domain.engine.frame.ComponentTypeEnum;
import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.Constant;
import com.missfresh.risk.bcp.domain.engine.frame.rss.ConfigChangeNotifier;
import com.missfresh.risk.bcp.domain.engine.frame.rss.domain.PublishParams;
import com.missfresh.risk.bcp.domain.entity.CronFieldEntity;
import com.missfresh.risk.bcp.domain.entity.EventSourceConfigEntity;
import com.missfresh.risk.bcp.domain.entity.InspectionEventSourceEntity;
import com.missfresh.risk.bcp.domain.exception.CheckParameterException;
import com.missfresh.risk.bcp.domain.exception.FailfastException;
import com.missfresh.risk.bcp.domain.script.SqlExecuteInvoker;
import com.missfresh.risk.bcp.domain.service.CronExpressDomainService;
import com.missfresh.risk.bcp.domain.service.EventSourceConfigDomainService;
import com.missfresh.risk.bcp.domain.service.InspectionEventSourceConfigDomainService;
import com.missfresh.risk.bcp.domain.service.InspectionLoadDataTemplateDomainService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.dto.InspectionEventSourceConfigDto;
import com.missfresh.risk.bcp.dto.LoadDataTemplateDto;
import com.missfresh.risk.bcp.dto.PageResultDto;
import com.missfresh.risk.bcp.enums.DispatchStrategyDefine;
import com.missfresh.risk.bcp.enums.LoadDataStrategyDefine;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
@Service
public class InspectionEventSourceConfigBizImpl implements InspectionEventSourceConfigService {
    private static final Logger logger = LoggerFactory.getLogger(InspectionEventSourceConfigBizImpl.class);
    /**
     * 不能禁用错误提示
     */
    private static final String ERROR_DISABLE_TIP = "该事件正在被有效规则使用, 不能禁用!";
    /**
     * cron表达式错误提示
     */
    private static final String ERROR_CRON_EXPRESSION_TIP = "生成cron表达式失败";
    /**
     * 不能禁用错误编码
     */
    private static final int ERROR_DISABLE_CODE = 20001;
    @Resource
    private CronExpressDomainService cronExpressDomainService;

    @Resource
    private EventSourceConfigDomainService eventSourceConfigDomainService;

    @Resource
    private InspectionEventSourceConfigDomainService iescdService;

    /**
     * 保存事件源配置对象
     *
     * @param iescDto
     * @return
     */
    @Override
    public Result<?> saveEventSource(final InspectionEventSourceConfigDto iescDto) {
        logger.info("保存事件:{}", JSONObject.toJSONString(iescDto));
        // 参数检查
        upsertInspectionEventSourceParamCheck(iescDto);
        // 参数转换
        InspectionEventSourceEntity entity = InspectionEventSourceEntitys.fromInspectionEventSourctDto(iescDto);
        // 保存事件源
        int effectRow = iescdService.saveEventSource(entity);
        // 构造结果
        Result<?> result = ResultUtils.wrapSuccess(effectRow);
        // 发送通知
        publishConfigChange(entity.getId(), PublishParams.ActionType.ADD);
        return result;

    }

    /**
     * 更新事件源配置对象类
     *
     * @param iescDto
     * @return
     */
    @Override
    public Result<?> updateEventSource(final InspectionEventSourceConfigDto iescDto) {
        logger.info("更新事件:{}", JSONObject.toJSONString(iescDto));
        // 参数检查
        upsertInspectionEventSourceParamCheck(iescDto);
        // 参数转换
        InspectionEventSourceEntity entity = InspectionEventSourceEntitys.fromInspectionEventSourctDto(iescDto);
        // 保存事件源
        int effectRow = iescdService.updateEventSource(entity);
        // 构造结果
        Result<?> result = ResultUtils.wrapSuccess(effectRow);
        // 发送通知
        publishConfigChange(entity.getId(), PublishParams.ActionType.UPDATE);
        return result;
    }


    @Override
    public Result<?> disable(InspectionEventSourceConfigDto iescDto) {
        // id参数检查
        eventSourceIdParamCheck(iescDto);
        // 禁用
        return disableAction(iescDto);
    }


    @Override
    public Result<?> enable(InspectionEventSourceConfigDto iescDto) {
        // 参数检查
        eventSourceIdParamCheck(iescDto);
        // 启用事件源配置
        return enableAction(iescDto);
    }


    @Override
    public Result<?> delete(InspectionEventSourceConfigDto iescDto) {
        // 参数检查
        eventSourceIdParamCheck(iescDto);
        // 删除事件源配置
        return deleteAction(iescDto);
    }


    @Override
    public Result<InspectionEventSourceConfigDto> info(Long id) {
        logger.info("获取详情:{}", id);
        if (null == id || id <= 0) {
            return ResultUtils.wrapSuccess(InspectionEventSourceConfigDto.builder().build());
        }
        // 获取事件源配置信息
        return getInspectionEventSourceConfigInfo(id);
    }


    @Override
    public Result<PageResultDto<InspectionEventSourceConfigDto>> page(final InspectionEventSourceConfigDto iescDto) {
        // 创建查询条件
        EventSourceConfigEntity selectWhereEntity = getSelectWhereEntity(iescDto);
        // 查询总记录条数
        Long pageTotal = iescdService.pageTotal(selectWhereEntity);
        // 查询数据
        PageResultDto<InspectionEventSourceConfigDto> pageResultDto = queryEventSourceConfig(selectWhereEntity, pageTotal);
        // 封装结果返回
        logger.info("query by page, condition:{}, result:{}", iescDto, pageResultDto);
        return Result.wrapSuccess(pageResultDto);
    }

    /**
     * 查询事件源配置记录数据
     *
     * @param whereEntity
     * @return
     */
    private PageResultDto<InspectionEventSourceConfigDto> queryEventSourceConfig(EventSourceConfigEntity whereEntity, Long pageTotal) {
        if (pageTotal == null && pageTotal < 0) {
            // 基本过滤后返回空数据分页
            return buildNullDataPage(pageTotal);
        }
        // 查询数据
        List<InspectionEventSourceEntity> pageData = iescdService.queryByPage(whereEntity);
        if (pageData == null) {
            // 基本过滤后返回空数据分页
            return buildNullDataPage(pageTotal);
        }
        // 返回带数据分页结构
        return buildDataPage(pageData, pageTotal);
    }

    /**
     * 创建空数据的分页结果
     *
     * @param pageTotal
     * @return
     */
    private PageResultDto<InspectionEventSourceConfigDto> buildNullDataPage(Long pageTotal) {
        return PageResultDto.<InspectionEventSourceConfigDto>builder()
                .total(pageTotal)
                .dataList(null)
                .build();
    }

    /**
     * 创建空数据的分页结果
     *
     * @param pageData
     * @param pageTotal
     * @return
     */
    private PageResultDto<InspectionEventSourceConfigDto> buildDataPage(List<InspectionEventSourceEntity> pageData, Long pageTotal) {
        List<InspectionEventSourceConfigDto> configDtos = pageData.stream().map(entity1 -> {
            InspectionEventSourceConfigDto configDto = InspectionEventSourceEntitys.toInspectionEventSourctDto(entity1);
            parseCron(configDto.getCron(), configDto);
            return configDto;
        }).collect(Collectors.toList());

        return PageResultDto.<InspectionEventSourceConfigDto>builder()
                .total(pageTotal)
                .dataList(configDtos)
                .build();
    }


    private EventSourceConfigEntity getSelectWhereEntity(InspectionEventSourceConfigDto iescDto) {
        EventSourceConfigEntity entity = EventSourceConfigEntitys.queryParamFromInspectionEventDto(iescDto);
        entity.setPageNo(iescDto.getPageNo());
        entity.setPageSize(iescDto.getPageSize());
        return entity;
    }

    /**
     * 查询分页数据
     *
     * @param iescDto
     * @return
     */
    private List<InspectionEventSourceEntity> queryPageData(InspectionEventSourceConfigDto iescDto) {
        EventSourceConfigEntity entity = EventSourceConfigEntitys.queryParamFromInspectionEventDto(iescDto);
        entity.setPageNo(iescDto.getPageNo());
        entity.setPageSize(iescDto.getPageSize());

        Long pageTotal = iescdService.pageTotal(entity);
        List<InspectionEventSourceEntity> pageData = null;
        if (pageTotal != null && pageTotal >= 0) {
            return iescdService.queryByPage(entity);
        }
        return null;
    }

    @Override
    public Result<?> mockScriptDataLoader(InspectionEventSourceConfigDto iescDto) {
        // 参数检查
        eventSourceConfigScriptParamCheck(iescDto);
        // 获取结果包装返回
        Object ret = SqlExecuteInvoker.mockInspectionScript(iescDto.getLoadDataScript().getLoadDataScriptSrc());
        return ResultUtils.wrapSuccess(JSONObject.toJSONString(ret));

    }

    /**
     * 脚本参数检查
     *
     * @param iescDto
     */
    private void eventSourceConfigScriptParamCheck(InspectionEventSourceConfigDto iescDto) {
        if (null == iescDto
                || null == iescDto.getLoadDataScript()
                || StringUtils.isEmpty(iescDto.getLoadDataScript().getLoadDataScriptSrc())) {
            throw new FailfastException(ErrorCodeEnum.ERR_UNKNOW_EXCEPTION.getCode(), "脚本为空");
        }
        mockScriptDataLoaderParamCheck(iescDto);
    }

    private void mockScriptDataLoaderParamCheck(final InspectionEventSourceConfigDto iescDto) {
        DispatchStrategyDefine dispatchStrategyDefine = DispatchStrategyDefine.fromStrategy(iescDto.getLoadDataScript().getDispatchStrategy());
        if (null == dispatchStrategyDefine) {
            throw new FailfastException(null, "请选择\"检查策略\"");
        }
    }

    @Resource
    private InspectionLoadDataTemplateDomainService inspectionLoadDataTemplateDomainService;

    @Override
    public Result<?> mockTemplateDataLoader(final LoadDataTemplateDto loadDataTemplateDto) {
        // 参数检查
        mockTemplateDataLoaderCheckParam(loadDataTemplateDto);
        // 获取结果并封装msg，返回
        Result<?> result = inspectionLoadDataTemplateDomainService.mockTemplateDataLoader(loadDataTemplateDto);
        result.setMsg(JSON.toJSONString(result.getData()));
        return result;

    }


    private void mockTemplateDataLoaderCheckParam(final LoadDataTemplateDto loadDataTemplateDto) {
        if (null == loadDataTemplateDto
                || StringUtils.isEmpty(loadDataTemplateDto.getFieldName())
                || StringUtils.isEmpty(loadDataTemplateDto.getConnMysqlAddr())
                || StringUtils.isEmpty(loadDataTemplateDto.getConnMysqlPort())
                || StringUtils.isEmpty(loadDataTemplateDto.getConnMysqlUsername())
                || StringUtils.isEmpty(loadDataTemplateDto.getConnMysqlPassword())
                || StringUtils.isEmpty(loadDataTemplateDto.getConnMysqlDatabase())
                || StringUtils.isEmpty(loadDataTemplateDto.getSql())
        ) {
            throw new FailfastException(null, "所有参数均不可为空");
        }

        SqlExecuteInvoker.sqlFormatCheck(loadDataTemplateDto.getSql());
    }

    @Override
    public Result<String> makeCronString(final InspectionEventSourceConfigDto iescDto) {
        // 获取作业表达式
        String cron = getCronStr(iescDto);
        // 包装返回
        return ResultUtils.wrapSuccess(cron);
    }

    /**
     * 设置作业字段信息
     *
     * @param cron
     * @param iescDto
     */
    private void parseCron(final String cron, InspectionEventSourceConfigDto iescDto) {
        try {
            // 正常设置作业字段
            setCronField(iescDto, cron);
        } catch (Exception e) {
            // 设置作业字段为空
            setCronFieldNull(iescDto, cron);
        }
    }


    /**
     * 获取作业表达式
     */
    private String getCronStr(InspectionEventSourceConfigDto iescDto) {
        final List<CronFieldEntity> entities = CronFieldEntitys.fromInspectionEventDto(iescDto);
        String cron = cronExpressDomainService.makeCronString(entities);
        if (StringUtils.isEmpty(cron)) {
            throw new FailfastException(ErrorCodeEnum.ERR_UNKNOW_EXCEPTION.getCode(), ERROR_CRON_EXPRESSION_TIP);
        }
        return cron;
    }

    /**
     * 获取事件源配置信息
     *
     * @param id
     * @return
     */
    @SneakyThrows
    private Result<InspectionEventSourceConfigDto> getInspectionEventSourceConfigInfo(Long id) {
        InspectionEventSourceEntity entity = iescdService.getById(id);
        InspectionEventSourceConfigDto configDto = InspectionEventSourceEntitys.toInspectionEventSourctDto(entity);
        parseCron(configDto.getCron(), configDto);
        return Result.wrapSuccess(configDto);
    }


    /**
     * 删除事件源配置
     *
     * @param iescDto
     * @return
     */
    private Result<?> deleteAction(InspectionEventSourceConfigDto iescDto) {
        EventSourceConfigEntity entity = InspectionEventSourceEntitys.fromInspectionEventSourctDto(iescDto);
        if (!eventSourceConfigDomainService.eventCloseable(entity)) {
            throw new FailfastException(ERROR_DISABLE_CODE, ERROR_DISABLE_TIP);
        }
        entity.setIsDelete(1);
        Result<?> result = ResultUtils.wrapSuccess(eventSourceConfigDomainService.updateByIdSelective(entity));
        publishConfigChange(entity.getId(), PublishParams.ActionType.DEL);
        return result;
    }

    /**
     * 启用事件源配置
     *
     * @param iescDto
     * @return
     */
    private Result<?> enableAction(InspectionEventSourceConfigDto iescDto) {
        EventSourceConfigEntity entity = InspectionEventSourceEntitys.fromInspectionEventSourctDto(iescDto);
        entity.setStatus(EventSourceStatusEnum.OPEN.getCode());
        Result<?> result = ResultUtils.wrapSuccess(eventSourceConfigDomainService.updateByIdSelective(entity));
        publishConfigChange(entity.getId(), PublishParams.ActionType.UPDATE);
        return result;
    }

    /**
     * 禁用事件源配置对象
     *
     * @param iescDto
     * @return
     */
    private Result<?> disableAction(InspectionEventSourceConfigDto iescDto) {
        EventSourceConfigEntity entity = InspectionEventSourceEntitys.fromInspectionEventSourctDto(iescDto);
        if (!eventSourceConfigDomainService.eventCloseable(entity)) {
            throw new FailfastException(ERROR_DISABLE_CODE, ERROR_DISABLE_TIP);
        }
        entity.setStatus(EventSourceStatusEnum.CLOSE.getCode());
        Result<?> result = ResultUtils.wrapSuccess(eventSourceConfigDomainService.updateByIdSelective(entity));
        publishConfigChange(entity.getId(), PublishParams.ActionType.UPDATE);
        return result;
    }

    /**
     * 更新或新增的事件源配置对象参数检查
     *
     * @param iescDto
     */
    private void upsertInspectionEventSourceParamCheck(final InspectionEventSourceConfigDto iescDto) {
        if (null == iescDto) {
            throw new FailfastException(null, "参数不能为空");
        }
        if (StringUtils.isEmpty(iescDto.getCron())) {
            throw new FailfastException(null, "cron配置错误，请先生成cron配置");
        }
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(iescDto.getLoadDataStrategy());
        if (null == loadDataStrategyDefine) {
            throw new FailfastException(null, "定时策略配置错误");
        }
        if (LoadDataStrategyDefine.GROOVY_SCRIPT == loadDataStrategyDefine) {
            Result<?> result = mockScriptDataLoader(iescDto);
            if (!ResultUtils.resultIsAvaliable(result)) {
                throw new FailfastException(null, "请确认数据加载脚本:" + result.getMsg());
            }
        }
        if (LoadDataStrategyDefine.TEMPLATE_MYSQL == loadDataStrategyDefine) {
            if (CollectionUtils.isEmpty(iescDto.getLoadDataTemplates())) {
                throw new FailfastException(null, "请增加模版配置");
            }
            for (LoadDataTemplateDto loadDataTemplate : iescDto.getLoadDataTemplates()) {
                Result<?> result = mockTemplateDataLoader(loadDataTemplate);
                if (!ResultUtils.resultIsAvaliable(result)) {
                    throw new FailfastException(null, "请确认模版配置:" + result.getMsg());
                }
            }
        }
    }

    /**
     * 事件源配置对象ID参数检查
     *
     * @param iescDto
     */
    private void eventSourceIdParamCheck(InspectionEventSourceConfigDto iescDto) {
        if (null == iescDto || null == iescDto.getId() || iescDto.getId() <= 0) {
            logger.info("参数错误:{}", JSON.toJSONString(iescDto));
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(), ErrorCodeEnum.ERR_PARAM.getMsg());
        }
    }

    /**
     * 设置事件源配置的作业字段
     *
     * @param iescDto
     * @param cron
     */
    private void setCronField(InspectionEventSourceConfigDto iescDto, String cron) {
        iescDto.setCron(cron);
        List<CronFieldEntity> fieldEntities = cronExpressDomainService.parseCronString(cron);
        iescDto.setCronSecendField(CronFieldEntitys.transToDto(fieldEntities.get(0)));
        iescDto.setCronMinuteField(CronFieldEntitys.transToDto(fieldEntities.get(1)));
        iescDto.setCronHourField(CronFieldEntitys.transToDto(fieldEntities.get(2)));
        iescDto.setCronDayField(CronFieldEntitys.transToDto(fieldEntities.get(3)));
        iescDto.setCronMonthField(CronFieldEntitys.transToDto(fieldEntities.get(4)));
    }

    /**
     * 设置事件源配置的作业字段为null
     *
     * @param iescDto
     * @param cron
     */
    private void setCronFieldNull(InspectionEventSourceConfigDto iescDto, String cron) {
        iescDto.setCron(cron);
        iescDto.setCronSecendField(null);
        iescDto.setCronMinuteField(null);
        iescDto.setCronHourField(null);
        iescDto.setCronDayField(null);
        iescDto.setCronMonthField(null);
    }


    /**
     * 事件源配置变更发送通知
     *
     * @param eventId
     * @param actionType
     */
    private void publishConfigChange(Long eventId, PublishParams.ActionType actionType) {
        PublishParams publishParams = new PublishParams();
        publishParams.setComponentTypeEnum(ComponentTypeEnum.EVENT);
        publishParams.setTypeId(String.valueOf(eventId));
        publishParams.setAction(actionType);
        ConfigChangeNotifier configChangeNotifier = AbstractApplicationContextUtil.getExtension(
                ConfigChangeNotifier.class,
                "redisRegistry");
        configChangeNotifier.doPublish(Constant.BROADCAST_ID_EVENT, JSON.toJSONString(publishParams));
        logger.info("发送变更通知:{}", JSON.toJSONString(publishParams));
    }


}
