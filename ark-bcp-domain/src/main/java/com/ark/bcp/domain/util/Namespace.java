package com.ark.bcp.domain.util;

/**
 * @author wangzheng@missfresh.cn on 2019-07-16
 */
public class Namespace {
    /**
     * .
     */
    public static final String ENGINE_CONFIG_LOG_PREFIX = "[ENGINE]";


    public static class RegitserConstants {
        public static final String TIMEOUT_KEY = "timeout";
        public static final int DEFAULT_TIMEOUT = 1000;

        /**
         * Period of registry center's retry interval
         */
        public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

        /**
         * Default value for the period of retry interval in milliseconds: 5000
         */
        public static final int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

        /**
         * Reconnection period in milliseconds for register center
         */
        public static final String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

        public static final int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;

        public static final String SESSION_TIMEOUT_KEY = "session";

        public static final int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    }
}
