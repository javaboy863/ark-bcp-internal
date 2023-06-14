package com.ark.bcp.domain.util;

import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wangzheng.
 */
public class ResultUtils {
    /**
     * 0:成功状态.
     */
    public static final int SUCCESS_CODE = 0;

    /***
     * @desc 包装成功的result.
     * @author yangzhenxing
     * @date 2018-08-01 18:13:53
     * @param data 返回值
     * @param <T> 返回值data部分数据类型
     * @return 结果信息
     */
    public static <T> Result<T> wrapSuccess(T data) {
        Result<T> result = new Result<T>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("");
        result.setData(data);
        return result;
    }

    /***
     * @desc 包装成功的result result 为  null.
     * @author yangzhenxing
     * @date 2018-08-01 18:14:05
     * @return 结果信息
     */
    public static Result<Object> wrapSuccess() {
        return wrapSuccess(null);
    }

    /***
     * @desc 包装失败的result.
     * @author yangzhenxing
     * @date 2018-08-01 18:14:30
     * @param code 错误码
     * @param msg 错误信息
     * @return 结果信息
     */
    public static <T> Result<T> wrapFailure(int code, String msg) {
        Result<T> result = new Result<>(code, msg);
        result.setData(null);
        return result;
    }

    /***
     * @desc 包装失败处理.
     * @author yangzhenxing
     * @date 2018-08-01 18:14:43
     * @param errorCodeDesc  错误类型
     * @return 结果信息
     */
    public static <T> Result<T> wrapFailure(ErrorCodeEnum errorCodeDesc) {
        return wrapFailure(errorCodeDesc.getCode(), errorCodeDesc.getMsg());
    }

    /***
     * @desc 包装失败处理, 报错信息有变量时候使用.
     * @author yangzhenxing
     * @date 2018-08-01 18:19:53
     * @param errorCodeDesc 错误类型
     * @param msgValues 错误码填充信息
     * @return 结果信息
     */
    public static <T> Result<T> wrapFailure(ErrorCodeEnum errorCodeDesc, String... msgValues) {
        String msg = errorCodeDesc.getMsg();
        if (msgValues != null && msgValues.length > 0) {
            msg = String.format(errorCodeDesc.getMsg(), msgValues);
        }
        return wrapFailure(errorCodeDesc.getCode(), msg);
    }

    /**
     * 包装失败，自定义拼接失败消息.
     *
     * @param errorCodeDesc 错误类型
     * @param failedMsg     拼接失败消息
     * @return 结果
     */
    public static Result wrapFailure(ErrorCodeEnum errorCodeDesc, String failedMsg) {
        String msg = errorCodeDesc.getMsg();
        if (StringUtils.isNotEmpty(failedMsg)) {
            msg = msg + ":" + failedMsg;
        }
        return wrapFailure(errorCodeDesc.getCode(), msg);
    }

    /**
     * 包装失败，既设置errorCode, 又设置data.
     *
     * @param errorCodeEnum 错误类型
     * @param data          错误数据
     * @param <T>           data数据
     * @return 结果信息
     * @author yangzhenxing
     * @date [2017-06-28 10:28:53]
     */
    public static <T> Result<T> wrapFailureWithData(ErrorCodeEnum errorCodeEnum, T data) {
        Result<T> result = wrapFailure(errorCodeEnum);
        result.setData(data);
        return result;
    }

    /**
     * @param exep 异常信息 .
     * @return 结果信息
     * @desc 打印错误并返回失败的系统级的Result，统一打印异常堆栈.
     * @author yangzhenxing
     * @date 2018-08-01 18:19:17
     */
    public static Result wrapException(Exception exep) {
        return wrapFailure(ErrorCodeEnum.ERR_SERVER_ERROR);
    }

    /**
     * 验证result是否有效.
     *
     * @param result resultObj
     * @return ""
     */
    public static boolean resultIsAvaliable(Result result) {
        if (null != result && result.isSuccess() && null != result.getData()) {
            return true;
        }
        return false;
    }
}

