

package com.ark.bcp.infr.cache.storage;

import com.missfresh.risk.bcp.domain.infrservice.PrivateStorageService;
import com.missfresh.shardingredis.command.Sedis;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 */
@Service(value = "privateStorageService")
public class PrivateStorageServiceImpl implements PrivateStorageService {
    @Resource(name = "common_storage_sedis")
    private Sedis commonStorage;

    @Override
    public void set(String key, String value, int ttlBySecend) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value) || ttlBySecend < 0) {
            return;
        }
        commonStorage.setex(key, ttlBySecend, value);
    }

    @Override
    public String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return commonStorage.get(key);
    }

    @Override
    public String hget(String key, String subkey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(subkey)) {
            return null;
        }
        return commonStorage.hget(key, subkey);
    }

    @Override
    public void hmset(String key, Map<String, String> kvs, int ttlBySecend) {
        if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(kvs)) {
            return;
        }
        commonStorage.hmset(key, kvs);
        commonStorage.expire(key, ttlBySecend);
    }
}
