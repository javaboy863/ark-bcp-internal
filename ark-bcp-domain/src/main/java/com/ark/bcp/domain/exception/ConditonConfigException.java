package com.ark.bcp.domain.exception;

/**
 */
public class ConditonConfigException extends RuntimeException {


    public ConditonConfigException() {
    }

    public ConditonConfigException(String message) {
        super(message);
    }

    public ConditonConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditonConfigException(Throwable cause) {
        super(cause);
    }

    public ConditonConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static ConditonConfigException newInstance(String message) {
        return new ConditonConfigException(message);
    }
}
