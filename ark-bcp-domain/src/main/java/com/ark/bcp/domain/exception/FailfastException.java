

package com.ark.bcp.domain.exception;

import lombok.Data;

/**
 */
@Data
public class FailfastException extends RuntimeException {
    private static final long serialVersionUID = 6759178218445830673L;
    private Object data;
    private String promotMsg;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public FailfastException(Object data, String promotMsg) {
        super(promotMsg);
        this.data = data;
        this.promotMsg = promotMsg;
    }
}
