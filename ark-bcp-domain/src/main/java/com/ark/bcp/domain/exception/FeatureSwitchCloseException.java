
package com.ark.bcp.domain.exception;

import lombok.Data;

/**
 */
@Data
public class FeatureSwitchCloseException extends RuntimeException {
    private static final long serialVersionUID = 945057332852193833L;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public FeatureSwitchCloseException() {
        super("开关关闭");
    }
}
