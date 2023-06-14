package com.ark.bcp.web.web.exceptionHandler;

import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.domain.exception.CheckParameterException;
import com.missfresh.risk.bcp.domain.exception.FailfastException;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @date 2022/01/10
 **/
@ControllerAdvice
public class ControllerExceptionHandle {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandle.class);

    @ExceptionHandler(CheckParameterException.class)
    public Result<?> handleUserNotExistException(CheckParameterException ex) {
        logger.error("CheckParameterException", ex);
        return ResultUtils.wrapFailure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(FailfastException.class)
    public Result<?> handleException(FailfastException e) {
        logger.error("调用抛快速失败异常", e);
        return ResultUtils.wrapFailure(ErrorCodeEnum.ERR_UNKNOW_EXCEPTION.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        logger.error("调用 异常", ex);
        return ResultUtils.wrapFailure(ErrorCodeEnum.ERR_UNKNOW_EXCEPTION);
    }


}


