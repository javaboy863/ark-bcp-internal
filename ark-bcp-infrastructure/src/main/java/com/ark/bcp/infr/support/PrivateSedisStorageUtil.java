

package com.ark.bcp.infr.support;

import com.missfresh.risk.bcp.domain.infrservice.PrivateStorageService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.Map;

/**
 */
public class PrivateSedisStorageUtil {
    public static void set(String key, String value, int ttlBySecend) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        PrivateStorageService privateStorageService = AbstractApplicationContextUtil.getExtension(PrivateStorageService.class, "PrivateStorageService");
        if (null != privateStorageService) {
            privateStorageService.set(key, value, ttlBySecend);
        }
    }

    public static String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        PrivateStorageService privateStorageService = AbstractApplicationContextUtil.getExtension(PrivateStorageService.class, "PrivateStorageService");
        if (null != privateStorageService) {
            return privateStorageService.get(key);
        }
        return null;
    }

    public static String hget(String key, String subkey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(subkey)) {
            return null;
        }
        PrivateStorageService privateStorageService = AbstractApplicationContextUtil.getExtension(PrivateStorageService.class, "PrivateStorageService");
        if (null != privateStorageService) {
            return privateStorageService.hget(key, subkey);
        }
        return null;
    }

    public static void hmset(String key, Map<String, String> kvs, int ttlBySecend) {
        if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(kvs)) {
            return;
        }
        PrivateStorageService privateStorageService = AbstractApplicationContextUtil.getExtension(PrivateStorageService.class, "PrivateStorageService");
        if (null != privateStorageService) {
            privateStorageService.hmset(key, kvs, ttlBySecend);
        }
    }
}
