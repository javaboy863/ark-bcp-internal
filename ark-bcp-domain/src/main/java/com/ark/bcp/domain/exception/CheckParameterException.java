package com.ark.bcp.domain.exception;

import com.missfresh.domain.ErrorCodeEnum;
import lombok.Data;

/**
 * 入参检查异常
 *
 **/
@Data
public class CheckParameterException extends RuntimeException {

    private static final long serialVersionUID = 3762643166601015414L;
    private int code;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CheckParameterException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CheckParameterException(String message) {
        super(message);
        this.code = ErrorCodeEnum.ERR_PARAM.getCode();
    }
}


