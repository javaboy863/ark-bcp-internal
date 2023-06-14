package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.RuleConfigService;
import com.missfresh.risk.bcp.domain.entity.BcpCheckRuleAlertEntity;
import com.missfresh.risk.bcp.domain.exception.CheckParameterException;
import com.missfresh.risk.bcp.domain.service.AlertMessageDomainService;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.domain.vo.AlertChannelParams;
import com.missfresh.risk.bcp.domain.vo.AlertMessageValueObject;
import com.missfresh.risk.bcp.dto.*;
import com.missfresh.risk.bcp.enums.RepairTypeDefine;
import com.mryx.grampus.ccs.dto.CcsLoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 */
@RestController
@RequestMapping("/risk/bcp/bg/rule")
@Slf4j
public class RuleConfigController {

    @Resource
    private RuleConfigService ruleConfigService;

    @Resource
    private AlertMessageDomainService alertMessageDomainService;

    @Resource
    private RestTemplate restTemplate;

    private final static int MOCK_AVAILBLE = 10001;


    /**
     * 保存和修改规则
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    public Result saveOrUpdateRuleConfig(@RequestBody JSONObject jsonObject) {
        RuleConfigDto ruleConfigDto = jsonObject.toJavaObject(RuleConfigDto.class);

        //检查是否可用
        checkMockAvailable(ruleConfigDto);

        //保存
        if (ruleConfigDto.getId() == null) {
            return saveRuleConfig(ruleConfigDto);
        }

        //修改
        return updateRuleConfig(ruleConfigDto);
    }

    /**
     * 查询规则
     *
     * @param queryDto
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public Result<PageResultDto<RuleConfigPageResultDto>> queryRuleConfig(@RequestBody RuleConfigPageQueryDto queryDto) {

        return ruleConfigService.queryRuleConfigList(queryDto);
    }

    /**
     * 删除规则
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result deleteRuleConfig(@RequestBody RuleConfigDto dto) {

        dto.setUpdatedBy(CcsLoginUser.get().getOauthName());

        return ruleConfigService.deleteRuleConfig(dto);
    }

    /**
     * 停用
     *
     * @param dto
     * @return
     */
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public Result disableRuleConfig(@RequestBody RuleConfigDto dto) {

        dto.setUpdatedBy(CcsLoginUser.get().getOauthName());

        return ruleConfigService.disableRuleConfig(dto);
    }

    /**
     * 启用
     *
     * @param ruleConfigDto
     * @return
     */
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public Result enableRuleConfig(@RequestBody RuleConfigDto ruleConfigDto) {

        return ruleConfigService.enableRuleConfig(setCcsLoginUser(ruleConfigDto));
    }

    /**
     * 获取详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<RuleConfigInfoDto> ruleInfo(Long id) {

        return ruleConfigService.ruleConfigInfo(id);
    }

    /**
     * 查询
     *
     * @param ruleConfigDto
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public Result<List<RuleConfigPageResultDto>> searchRules(@RequestBody RuleConfigDto ruleConfigDto) {

        return ruleConfigService.searchRuleConfigList(ruleConfigDto);
    }

    /**
     * mock
     *
     * @param ruleCodeValidateDto
     * @return
     */
    @RequestMapping(value = "/mock", method = RequestMethod.POST)
    public Result mock(@RequestBody RuleCodeValidateDto ruleCodeValidateDto) {

        //检查入参
        checkMockRequest(ruleCodeValidateDto);

        //转换入参
        RuleConfigDto ruleConfigDto = convertToRuleConfigDto(ruleCodeValidateDto);

        //调用服务
        Result<List<MokeResultDto>> listResult = ruleConfigService.mockTest(ruleConfigDto);

        //处理返回结果
        return processMockRes(listResult);
    }

    /**
     * try Alert
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/tryAlert", method = RequestMethod.POST)
    public Result tryAlert(@RequestBody JSONObject jsonObject) {

        RuleConfigDto ruleConfigDto = jsonObject.toJavaObject(RuleConfigDto.class);
        //入参检查
        tryAlertParamCheck(ruleConfigDto);
        //请求服务
        tryAlertDoSend(ruleConfigDto);

        return ResultUtils.wrapSuccess();
    }

    /**
     * try Repair
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/tryRepair", method = RequestMethod.POST)
    public Result tryRepair(@RequestBody JSONObject jsonObject) {

        RuleConfigDto ruleConfigDto = jsonObject.toJavaObject(RuleConfigDto.class);
        //检查入参
        tryRepairParamCheck(ruleConfigDto);
        //请求
        return send(ruleConfigDto);
    }


    /**
     * 入参检查
     *
     * @param ruleConfigDto
     */
    private void tryAlertParamCheck(RuleConfigDto ruleConfigDto) {
        if (null == ruleConfigDto || null == ruleConfigDto.getAlertConfigDto()) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(), ErrorCodeEnum.ERR_PARAM.getMsg());
        }
    }

    /**
     * 请求
     *
     * @param ruleConfigDto
     */
    private void tryAlertDoSend(RuleConfigDto ruleConfigDto) {
        AlertMessageValueObject message = AlertMessageValueObject.transAlertMsg(
                ruleConfigDto.getAlertConfigDto().getAlertTextFormat(),
                ruleConfigDto.getMockData(),
                ruleConfigDto.getRuleName());

        AlertConfigDto alertConfigDto = ruleConfigDto.getAlertConfigDto();
        BcpCheckRuleAlertEntity alertEntity = BcpCheckRuleAlertEntity.builder()
                .alertType(alertConfigDto.getAlertChannelCode())
                .appCode(ruleConfigDto.getAppCode())
                .alertTextFormat(alertConfigDto.getAlertTextFormat())
                .alertConfigJson(JSON.toJSONString(AlertChannelParams.builder().alertUrl(alertConfigDto.getAlertGroupChatUrl()).build()))
                .build();
        alertMessageDomainService.send(message, alertEntity);
    }

    /**
     * 入参检查
     *
     * @param ruleConfigDto
     */
    private void tryRepairParamCheck(RuleConfigDto ruleConfigDto) {
        if (null == ruleConfigDto || null == ruleConfigDto.getRepairConfigDto()) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(), ErrorCodeEnum.ERR_PARAM.getMsg());
        }
    }

    /**
     * 检查是否可用
     *
     * @param ruleConfigDto
     */
    private void checkMockAvailable(RuleConfigDto ruleConfigDto) {
        Result<List<MokeResultDto>> mockListResult = ruleConfigService.mockTest(ruleConfigDto);

        for (int i = 0; i < mockListResult.getData().size(); i++) {
            MokeResultDto mokeResultDto = mockListResult.getData().get(i);
            if (!mokeResultDto.getAvailable()) {
                log.info("验证失败:{}", mokeResultDto);
                throw new CheckParameterException(MOCK_AVAILBLE, "第" + (i + 1) + "规则验证失败:" + mokeResultDto.getPromopt());
            }
        }

    }

    /**
     * 保存
     *
     * @param ruleConfigDto
     * @return
     */
    private Result saveRuleConfig(RuleConfigDto ruleConfigDto) {

        ruleConfigDto.setUpdatedBy(null == CcsLoginUser.get() ? "null" : CcsLoginUser.get().getOauthName());
        return ruleConfigService.saveRuleConfig(ruleConfigDto);
    }

    /**
     * 修改
     *
     * @param ruleConfigDto
     * @return
     */
    private Result updateRuleConfig(RuleConfigDto ruleConfigDto) {

        ruleConfigDto.setUpdatedBy(null == CcsLoginUser.get() ? "null" : CcsLoginUser.get().getOauthName());
        return ruleConfigService.updateRuleConfig(ruleConfigDto);
    }

    /**
     * 组装当前登陆用户信息
     *
     * @param ruleConfigDto
     * @return
     */
    private RuleConfigDto setCcsLoginUser(RuleConfigDto ruleConfigDto) {

        RuleConfigDto dto = RuleConfigDto.builder()
                .updatedBy(CcsLoginUser.get().getOauthName())
                .id(ruleConfigDto.getId())
                .eventId(ruleConfigDto.getEventId())
                .build();


        return dto;
    }

    /**
     * 检查入参
     *
     * @param ruleCodeValidateDto
     */
    private void checkMockRequest(RuleCodeValidateDto ruleCodeValidateDto) {

        if (null == ruleCodeValidateDto) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(), "入参不正确");
        }
    }

    /**
     * 转换入参
     *
     * @param ruleCodeValidateDto
     * @return
     */
    private RuleConfigDto convertToRuleConfigDto(RuleCodeValidateDto ruleCodeValidateDto) {
        List<RuleCodeDto> ruleCodeDtoList = new ArrayList<>();
        ruleCodeDtoList.add(RuleCodeDto.builder()
                .scriptContent(ruleCodeValidateDto.getScriptContent())
                .codeType(ruleCodeValidateDto.getCodeType())
                .build());

        RuleConfigDto ruleConfigDto = RuleConfigDto.builder()
                .mockData(ruleCodeValidateDto.getMockData())
                .codeList(ruleCodeDtoList)
                .build();

        return ruleConfigDto;
    }

    /**
     * 处理返回结果
     *
     * @param listResult
     * @return
     */
    private Result processMockRes(Result<List<MokeResultDto>> listResult) {

        Result result = null;
        if (listResult.isSuccess()) {
            result = Result.wrapSuccess(listResult.getData().get(0));
            result.setMsg(listResult.getData().get(0).getPromopt());
        } else {
            result = listResult;
        }
        return result;
    }

    private Result send(RuleConfigDto ruleConfigDto) {
        RepairConfigDto repairConfigDto = ruleConfigDto.getRepairConfigDto();

        if (RepairTypeDefine.NO_REPAIR.getCode().equals(repairConfigDto.getRepairTypeCode())) {
            return wrapSuccess("无需触发回调", "无需触发回调");
        }
        //http 请求
        if (RepairTypeDefine.HTTP_CALLBACK_REPAIR.getCode().equals(repairConfigDto.getRepairTypeCode())) {
            return sendHttp(ruleConfigDto);
        }

        return ResultUtils.wrapFailure(-1, "不识别的回调方式");
    }

    /**
     * http 请求
     *
     * @param ruleConfigDto
     * @return
     */
    private Result sendHttp(RuleConfigDto ruleConfigDto) {

        RepairConfigDto repairConfigDto = ruleConfigDto.getRepairConfigDto();
        if (StringUtils.isEmpty(repairConfigDto.getHttpCallbackUrl())) {
            return ResultUtils.wrapFailure(-1, "请配置http回调地址");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> request = new HttpEntity<>(ruleConfigDto.getMockData(), headers);
        ResponseEntity<String> postForEntity = restTemplate.postForEntity(repairConfigDto.getHttpCallbackUrl(), request, String.class);
        log.info("发送报警消息,返回:{},{}", postForEntity.getStatusCodeValue(), postForEntity.getBody());
        if (!postForEntity.getStatusCode().is2xxSuccessful()) {
            return ResultUtils.wrapFailure(-1, "callback invoke code:" + postForEntity.getStatusCodeValue());
        }
        return ResultUtils.wrapSuccess(postForEntity.getBody());
    }


    private Result wrapSuccess(String data, String msg) {
        Result result = ResultUtils.wrapSuccess(data);
        result.setMsg(msg);
        return result;
    }


}
