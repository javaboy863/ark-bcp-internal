
package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.InspectionEventSourceConfigService;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.dto.InspectionEventSourceConfigDto;
import com.missfresh.risk.bcp.dto.LoadDataTemplateDto;
import com.missfresh.risk.bcp.dto.PageResultDto;
import com.mryx.grampus.ccs.dto.CcsLoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 */
@RestController
@RequestMapping(path = {"/risk/bcp/bg/insp/event"})
public class InspectionEventSourceConfigController {
    private static final Logger logger = LoggerFactory.getLogger(InspectionEventSourceConfigController.class);

    @Resource
    private InspectionEventSourceConfigService inspectionEventSourceConfigService;

    @RequestMapping(value = "/mockScriptDataLoader", method = RequestMethod.POST)
    public Result<?> mockScriptDataLoader(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("mockScriptDataLoader, dto:{},by:{}", JSON.toJSONString(iescDto), getLoginUserName());
        return inspectionEventSourceConfigService.mockScriptDataLoader(iescDto);
    }

    @RequestMapping(value = "/mockTemplateDataLoader", method = RequestMethod.POST)
    public Result<?> mockTemplateDataLoader(@RequestBody LoadDataTemplateDto iescDto) {
        logger.info("mockTemplateDataLoader, dto:{},by:{}", JSON.toJSONString(iescDto), getLoginUserName());
        return inspectionEventSourceConfigService.mockTemplateDataLoader(iescDto);
    }

    @RequestMapping(value = "/makeCronString", method = RequestMethod.POST)
    public Result<JSONObject> makeCronString(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("makeCronString, dto:{},by:{}", JSON.toJSONString(iescDto), getLoginUserName());
        return makeCronStringResult(iescDto);
    }

    /**
     * 获取定时字符串
     *
     * @param iescDto
     * @return
     */
    private Result<JSONObject> makeCronStringResult(InspectionEventSourceConfigDto iescDto) {
        Result<String> cronResult = inspectionEventSourceConfigService.makeCronString(iescDto);
        if (!ResultUtils.resultIsAvaliable(cronResult)) {
            return ResultUtils.wrapFailure(cronResult.getCode(), cronResult.getMsg());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cron", cronResult.getData());
        return ResultUtils.wrapSuccess(jsonObject);
    }

    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    public Result<?> saveOrUpdate(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("saveOrUpdate, dto:{},by:{}", JSON.toJSONString(iescDto), getLoginUserName());
        return createOrUpdateEventSource(iescDto);
    }

    /**
     * 新增或更新检查事件源配置
     *
     * @param iescDto
     * @return
     */
    private Result<?> createOrUpdateEventSource(InspectionEventSourceConfigDto iescDto) {
        if (null == iescDto.getId() || 0 == iescDto.getId()) {
            return createEventSource(iescDto);
        }
        return updateEventSource(iescDto);
    }

    /**
     * 事件源配置会不可用
     *
     * @param iescDto
     * @return
     */
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public Result<?> disableEventSource(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("disable inspectionEventSource:{}", iescDto.getId());
        setUpdateUser(iescDto);
        return inspectionEventSourceConfigService.disable(iescDto);
    }

    /**
     * 事件源配置会不可用
     *
     * @param iescDto
     * @return
     */
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public Result<?> enableEventSource(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("enable inspectionEventSource:{}", iescDto.getId());
        setUpdateUser(iescDto);
        return inspectionEventSourceConfigService.enable(iescDto);
    }

    /**
     * 删除事件源
     *
     * @param iescDto
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result<?> delete(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("delete inspectionEventSource:{}", iescDto.getId());
        setUpdateUser(iescDto);
        return inspectionEventSourceConfigService.delete(iescDto);
    }

    /**
     * 事件源配置删除
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<InspectionEventSourceConfigDto> info(Long id) {
        logger.info("detail inspectionEventSource:{}", id);
        return inspectionEventSourceConfigService.info(id);
    }

    /**
     * 分页查询事件源配置信息
     *
     * @param iescDto
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public Result<PageResultDto<InspectionEventSourceConfigDto>> page(@RequestBody InspectionEventSourceConfigDto iescDto) {
        logger.info("page query:{}", iescDto);
        return inspectionEventSourceConfigService.page(iescDto);
    }

    /**
     * 更新检查事件源配置
     *
     * @param iescDto
     * @return
     */
    private Result<?> updateEventSource(InspectionEventSourceConfigDto iescDto) {
        setUpdateUser(iescDto);
        return inspectionEventSourceConfigService.updateEventSource(iescDto);
    }

    /**
     * 新增事件源
     *
     * @param iescDto
     * @return
     */
    private Result<?> createEventSource(InspectionEventSourceConfigDto iescDto) {
        iescDto.setCreateUser(getLoginUserName());
        return inspectionEventSourceConfigService.saveEventSource(iescDto);
    }

    /**
     * 检查事件源配置对象设置更新时间
     *
     * @param iescDto
     */
    private void setUpdateUser(InspectionEventSourceConfigDto iescDto) {
        iescDto.setUpdateUser(getLoginUserName());
    }

    /**
     * 获取当前登录用户的用户名
     *
     * @return
     */
    private String getLoginUserName() {
        return null == CcsLoginUser.get() ? "null" : CcsLoginUser.get().getOauthName();
    }
}
