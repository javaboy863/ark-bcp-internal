

package com.ark.bcp.domain.exception;

import lombok.Data;

/**
 */
@Data
public class IllegalParamException extends RuntimeException {
    private static final long serialVersionUID = 3762643166601015414L;
    private int code;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public IllegalParamException(int code, String message) {
        super(message);
        this.code = code;
    }

    public IllegalParamException(String message) {
        super(message);
        this.code = 40000 + 1;
    }
}
