package com.ark.bcp.domain.util;

import com.mryx.sentinel.Entry;
import com.mryx.sentinel.SphU;
import com.mryx.sentinel.slots.block.BlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author wangzheng
 */
public class SentinelUtil {
    private static Logger logger = LoggerFactory.getLogger(SentinelUtil.class);

    private static final String DEGRADATION_LOG_PREFIX = "Sentinel_Deg";

    /**
     * 通用降级包装方法.
     *
     * @param name         ""
     * @param supplier     ""
     * @param defaultValue ""
     * @param <T>          ""
     * @return ""
     */
    public static <T> T wrapSimpleInterface(
            String name,
            Supplier<T> supplier,
            Supplier<T> defaultValue) {

        long lStartTime = System.currentTimeMillis();
        Entry entry = null;
        try {
            entry = SphU.entry(name);
            return supplier.get();

        } catch (Exception e) {
            if (BlockException.isBlockException(e)) {
                logger.info(DEGRADATION_LOG_PREFIX + ",totol:{}", name, defaultValue.get(), "",
                        System.currentTimeMillis() - lStartTime);
            } else {
                logger.error("wrapSimpleInterface error.name:{}", name, e);
            }
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return defaultValue.get();
    }
}

