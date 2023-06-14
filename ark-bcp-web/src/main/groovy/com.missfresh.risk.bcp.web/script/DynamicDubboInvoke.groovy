package com.missfresh.risk.bcp.web.script

import com.alibaba.dubbo.config.ApplicationConfig
import com.alibaba.dubbo.config.ReferenceConfig
import com.alibaba.dubbo.config.RegistryConfig
import com.alibaba.dubbo.config.utils.ReferenceConfigCache
import com.alibaba.dubbo.rpc.service.GenericService
import com.google.common.base.Stopwatch
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.missfresh.as.log.Logger
import com.missfresh.as.log.LoggerFactory
import com.mryx.monitor.api.BusinessMonitor

import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class DynamicDubboInvoke {
    private static final Logger logger = LoggerFactory.getLogger(DynamicDubboInvoke.class);

    private final static Cache<String, WeakReference<ReferenceConfig<GenericService>>> REFERENCECONFIG_CACHE =
            CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(60, TimeUnit.MINUTES).build();

    private final static Object REFERENCECONFIG_CACHE_LOCK_OBJECT = new Object();

    private static Properties groovyEnvProperties = null;

    static void initEnv(Properties properties) {
        groovyEnvProperties = properties;
    }

    static String getZkAddrFromProperties() {
        if (null == groovyEnvProperties) {
            logger.error("load config fail, from : groovyEnv.properties");
            return;
        }
        logger.info("load config over size: {}", groovyEnvProperties);
        return groovyEnvProperties.getProperty("zkaddrs");
    }

    static ReferenceConfig<GenericService> genericServiceConfig(Map<String, Object> referenceProperties) {
        // 当前dubbo consumer的application配置，不设置会直接抛异常
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("risk_test_groovy");
        // 注册中心配置
        RegistryConfig registryConfig = new RegistryConfig();
        // 注册中心这里需要配置上注册中心协议，例如下面的zookeeper
        registryConfig.setProtocol("zookeeper");
        registryConfig.setAddress(referenceProperties.getOrDefault("zkaddrs", getZkAddrFromProperties()) as String);
        logger.info("zkaddrs:{}", registryConfig.getAddress());
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);

        // 设置调用的reference属性，下面只设置了协议、接口名、版本、超时时间
        reference.setProtocol((String) referenceProperties.getOrDefault("protocol", "dubbo"));
        reference.setInterface((String) referenceProperties.getOrDefault("interface", ""));
        reference.setVersion((String) referenceProperties.getOrDefault("version", "*"));
        reference.setTimeout((Integer) referenceProperties.getOrDefault("timeout", 1000));
        reference.setRetries((Integer) referenceProperties.getOrDefault("retries", 0));
        reference.setGroup((String) referenceProperties.getOrDefault("group", null));
        reference.setGeneric(true);
        logger.info("reference:{}", reference.toString());
        return reference;
    }


    static ReferenceConfig<GenericService> getOrNewReferenceConfig(String interfaceName) {
        if (null == interfaceName || "".equalsIgnoreCase(interfaceName)) {
            return null;
        }

        Map<String, Object> referenceProperties = new HashMap<>(8);
        referenceProperties.put("interface", interfaceName);
        return getOrNewReferenceConfig(referenceProperties);
    }

    static ReferenceConfig<GenericService> getOrNewReferenceConfig(Map<String, Object> referenceProperties) {
        synchronized (REFERENCECONFIG_CACHE_LOCK_OBJECT) {
            String refConfigCacheKey = (String) referenceProperties.get("interface");
            WeakReference<ReferenceConfig<GenericService>> referenceConfigWeakReference = REFERENCECONFIG_CACHE.getIfPresent(refConfigCacheKey);

            if (referenceConfigWeakReference != null) {//缓存有弱引用
                ReferenceConfig<GenericService> referenceConfigFromWR = referenceConfigWeakReference.get();
                if (referenceConfigFromWR == null) {//证明没人引用自己被GC了，需要重建
                    ReferenceConfig<GenericService> referenceConfig = genericServiceConfig(referenceProperties);
                    REFERENCECONFIG_CACHE.put(refConfigCacheKey, new WeakReference<>(referenceConfig));
                    //放入缓存中，用弱应用hold住，不影响该有GC
                    return referenceConfig;
                } else {
                    return referenceConfigFromWR;
                }

            } else {//缓存没有，则创建
                ReferenceConfig<GenericService> referenceConfig = genericServiceConfig(referenceProperties);
                REFERENCECONFIG_CACHE.put(refConfigCacheKey, new WeakReference<>(referenceConfig));
                //放入缓存中，用弱应用hold住，不影响该有GC
                return referenceConfig;
            }
        }
    }

    static GenericService getGenericService(ReferenceConfig<GenericService> referenceConfig) {
        if (null == referenceConfig) {
            return null;
        }
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        // cache.get方法中会缓存 Reference对象，并且调用ReferenceConfig.get方法启动ReferenceConfig
        GenericService genericService = cache.get(referenceConfig);
        return genericService;
    }

    static Object invokeDubboSimplified(
            String interfaceName, String methodName, String[] argTypes, Object[] argParams
    ) {
        ReferenceConfig<GenericService> referenceConfig = getOrNewReferenceConfig(interfaceName);
        GenericService genericService = getGenericService(referenceConfig);
        return innerInvoke(interfaceName, genericService, methodName, argTypes, argParams);
    }

    static Object invokeDubboWithReferenceProperties(
            Map<String, Object> referenceProperties,
            String methodName, String[] argTypes, Object[] argParams
    ) {
        ReferenceConfig<GenericService> referenceConfig = getOrNewReferenceConfig(referenceProperties);
        GenericService genericService = getGenericService(referenceConfig);

        String interfaceName = (String) referenceProperties.get("interface");
        return innerInvoke(interfaceName, genericService, methodName, argTypes, argParams);
    }

    static Object innerInvoke(String interfaceName,
                              GenericService genericService, String methodName,
                              String[] argTypes, Object[] argParams) {
        Object result = null;
        Long startTm = System.currentTimeMillis();
        try {
            result = genericService.$invoke(methodName, argTypes, argParams);
        } catch (Exception e) {
            BusinessMonitor.recordOne("busi_dubbo_" + interfaceName + "_" + methodName + "_error");
            throw e;
        } finally {
            BusinessMonitor.recordOne("busi_dubbo_" + interfaceName + "_" + methodName, System.currentTimeMillis() - startTm);
        }
        return result;
    }

    static Object healthCheck() {
        return true;
    }
}