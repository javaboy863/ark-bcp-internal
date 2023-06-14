

package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.AlertRecordService;
import com.missfresh.risk.bcp.domain.exception.CheckParameterException;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.dto.AlertRecordDto;
import com.missfresh.risk.bcp.dto.AlertRecordPageQueryDto;
import com.missfresh.risk.bcp.dto.PageResultDto;
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
@RequestMapping({"/risk/bcp/bg/alert"})
public class AlertRecordController {
    private static final Logger logger = LoggerFactory.getLogger(AlertRecordController.class);

    @Resource
    private AlertRecordService alertRecordService;

    /**
     * 分页查找告警.
     *
     * @param jsonObject ""
     * @return ""
     */
    @RequestMapping(value = "/listByPage", method = RequestMethod.POST)
    public Result<PageResultDto<AlertRecordDto>> recordListByPage(@RequestBody JSONObject jsonObject) {
        logger.info("recv listbypage {}", jsonObject.toJSONString());
        AlertRecordPageQueryDto pageQueryDto = jsonObject.toJavaObject(AlertRecordPageQueryDto.class);
        //检查入参
        recordListByPageCheckReq(pageQueryDto);

        return alertRecordService.queryByPage(pageQueryDto);
    }

    /**
     * 更新 alert.
     *
     * @param jsonObject ""
     * @return ""
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Result updateHandleMsg(@RequestBody JSONObject jsonObject) {
        logger.info("recv update {}", jsonObject.toJSONString());
        AlertRecordDto alertRecordDto = jsonObject.toJavaObject(AlertRecordDto.class);
        //检查入参
        updateHandleMsgCheckReq(alertRecordDto);

        return alertRecordService.updateAlertRecord(alertRecordDto);
    }

    /**
     * 报警项详情.
     *
     * @param jsonObject ""
     * @return ""
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    public Result<AlertRecordDto> alertDetail(@RequestBody JSONObject jsonObject) {
        logger.info("recv detail {}", jsonObject.toJSONString());
        AlertRecordDto alertRecordDto = jsonObject.toJavaObject(AlertRecordDto.class);

        updateHandleMsgCheckReq(alertRecordDto);

        return alertRecordService.detail(alertRecordDto.getId());
    }


    /**
     * 检查入参
     * @param pageQueryDto
     */
    private void recordListByPageCheckReq(AlertRecordPageQueryDto pageQueryDto){
        if (null == pageQueryDto) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(),"格式不正确");
        }
    }

    /**
     * 检查入参
     * @param alertRecordDto
     */
    private void  updateHandleMsgCheckReq(AlertRecordDto alertRecordDto){
        if (null == alertRecordDto) {
            throw new CheckParameterException(ErrorCodeEnum.ERR_PARAM.getCode(),"格式不正确");
        }
    }

}
