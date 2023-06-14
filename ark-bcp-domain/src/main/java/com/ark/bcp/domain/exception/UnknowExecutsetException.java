
package com.ark.bcp.domain.exception;

/**
 */
public class UnknowExecutsetException extends RuntimeException {


    public UnknowExecutsetException() {
    }

    public UnknowExecutsetException(String message) {
        super(message);
    }

    public UnknowExecutsetException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknowExecutsetException(Throwable cause) {
        super(cause);
    }

    public UnknowExecutsetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static UnknowExecutsetException newInstance(String message) {
        return new UnknowExecutsetException(message);
    }
}
