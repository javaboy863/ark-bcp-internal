
package com.ark.bcp.domain.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 */
public class MapPathUtil {
    public static String parse(final String path, final Map<String, Object> data) {
        Object object = parse0(path, data);
        return null == object ? null : String.valueOf(object);
    }

    public static Object parseObject(final String path, final Map<String, Object> data) {
        return parse0(path, data);
    }

    public static Boolean parseBoolean(final String path, final Map<String, Object> data) {
        Object object = parse0(path, data);
        return null == object ? null : Boolean.valueOf(String.valueOf(object));
    }

    public static Long parseLong(final String path, final Map<String, Object> data) {
        Object object = parse0(path, data);
        return null == object ? null : Long.valueOf(String.valueOf(object));
    }


    @SuppressWarnings("AlibabaUndefineMagicConstant")
    private static Object parse0(final String path, final Map<String, Object> data) {
        if (null == path || path.isEmpty()) {
            return null;
        }
        if (null == data || data.isEmpty()) {
            return null;
        }
        String key = path;
        String subPath = null;
        if (path.contains(".")) {
            int pointPos = path.indexOf('.');
            key = path.substring(0, pointPos);
            if (path.length() > pointPos + 1) {
                subPath = path.substring(path.indexOf(".") + 1);
            }
        }
        if (!data.containsKey(key)) {
            return null;
        }
        Object object = data.get(key);
        if (null == object) {
            return null;
        }
        if (object instanceof Map && !StringUtils.isEmpty(subPath)) {
            return parse0(subPath, (Map<String, Object>) object);
        }

        return object;
    }
}
