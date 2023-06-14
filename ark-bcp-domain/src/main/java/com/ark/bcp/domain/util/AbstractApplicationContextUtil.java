package com.ark.bcp.domain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class AbstractApplicationContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(AbstractApplicationContextUtil.class);

    private static final Map<ApplicationContext, Object> CONTEXTS = new ConcurrentHashMap<>();

    public static void addApplicationContext(ApplicationContext context) {
        if (!CONTEXTS.containsKey(context)) {
            CONTEXTS.put(context, new Object());
        }
    }

    public static void removeApplicationContext(ApplicationContext context) {
        CONTEXTS.remove(context);
    }

    public static Map<ApplicationContext, Object> getContexts() {
        return CONTEXTS;
    }

    public static void clearContexts() {
        CONTEXTS.clear();
    }

    public static <T> T getExtension(Class<T> type, String name) {
        if (!StringUtils.isEmpty(name)) {
            for (Map.Entry<ApplicationContext, Object> entry : CONTEXTS.entrySet()) {
                if (entry.getKey().containsBean(name)) {
                    Object bean = entry.getKey().getBean(name);
                    if (type.isInstance(bean)) {
                        return (T) bean;
                    }
                }
            }
        }


        if (Object.class == type) {
            return null;
        }

        for (Map.Entry<ApplicationContext, Object> entry : CONTEXTS.entrySet()) {
            try {
                return entry.getKey().getBean(type);
            } catch (NoUniqueBeanDefinitionException multiBeanExe) {
                logger.error("Find more than 1 spring extensions (beans) of type " + type.getName() + ", will stop auto injection. Please make sure you have specified the concrete parameter type and there's only one extension of that type.");
            } catch (NoSuchBeanDefinitionException noBeanExe) {
                logger.error("Error when get spring extension(bean) for type:" + type.getName(), noBeanExe);
            }
        }
        logger.warn("No spring extension (bean) named:" + name + ", type:" + type.getName() + " found, stop get bean.");
        return null;
    }
}
