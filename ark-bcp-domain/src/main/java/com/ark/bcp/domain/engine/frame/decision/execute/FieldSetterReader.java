

package com.ark.bcp.domain.engine.frame.decision.execute;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 */
public class FieldSetterReader {
    private static Logger logger = LoggerFactory.getLogger(FieldSetterReader.class);

    public static class Setter {

        public static void setFeild(Map<String, String> params, String key, String value) {
            if (null == params || StringUtils.isEmpty(key)) {
                return;
            }
            params.put(key, value);
        }

        public static void setRuntimeField(Map<String, Object> refMap, String key, Object value) {
            if (null == refMap || StringUtils.isEmpty(key)) {
                return;
            }
            refMap.put(key, value);
        }

        public static void setRuntimeField(ContextWrap contextWrap, String key, Object value) {
            if (null == contextWrap || null == contextWrap.getRuntimeContext() || StringUtils.isEmpty(key)) {
                return;
            }
            contextWrap.getRuntimeContext().put(key, value);
        }
    }

    public static class Reader {
        public static String getStringField(final Map<String, String> context, final String key) {
            if (MapUtils.isEmpty(context) || StringUtils.isEmpty(key) || !context.containsKey(key)) {
                return null;
            }
            return context.get(key);
        }

        public static String getStringField(final ContextWrap contextWrap, final String key) {
            if (null == contextWrap
                    || MapUtils.isEmpty(contextWrap.getContext())
                    || StringUtils.isEmpty(key) || !contextWrap.getContext().containsKey(key)) {
                return null;
            }
            return contextWrap.getContext().get(key);
        }

        public static Long getLongField(final ContextWrap contextWrap, final String key) {
            if (null == contextWrap
                    || MapUtils.isEmpty(contextWrap.getContext())
                    || StringUtils.isEmpty(key) || !contextWrap.getContext().containsKey(key)) {
                return null;
            }
            return transtoLong(contextWrap.getContext().get(key));
        }

        public static String getRuntimeStringField(final Map<String, Object> context, final String key) {
            if (MapUtils.isEmpty(context) || StringUtils.isEmpty(key) || !context.containsKey(key)) {
                return null;
            }
            final Object value = context.get(key);
            if (null == value) {
                return null;
            }
            if (value instanceof String) {
                return (String) value;
            }
            return String.valueOf(value);
        }

        public static String getRuntimeStringField(final ContextWrap contextWrap, final String key) {
            if (null == contextWrap
                    || MapUtils.isEmpty(contextWrap.getRuntimeContext())
                    || StringUtils.isEmpty(key)
                    || !contextWrap.getRuntimeContext().containsKey(key)) {
                return null;
            }
            final Object value = contextWrap.getRuntimeContext().get(key);
            if (null == value) {
                return null;
            }
            if (value instanceof String) {
                return (String) value;
            }
            return String.valueOf(value);
        }

        public static Long getRuntimeLongField(final Map<String, Object> context, final String key) {
            if (MapUtils.isEmpty(context) || StringUtils.isEmpty(key) || !context.containsKey(key)) {
                return null;
            }
            final Object value = context.get(key);
            if (null == value) {
                return null;
            }
            if (value instanceof Long) {
                return (Long) value;
            }

            return transtoLong(value);
        }

        private static Long transtoLong(Object longStr) {
            Long retLong = null;
            try {
                if (longStr instanceof Long) {
                    return (Long) longStr;
                }
                if (longStr instanceof String) {
                    retLong = Long.parseLong((String) longStr);
                } else {
                    retLong = Long.parseLong(String.valueOf(longStr));
                }
            } catch (Exception e) {
                // donothing
                logger.error("解析long失败,{}", longStr);
            }
            return retLong;
        }
    }


}
