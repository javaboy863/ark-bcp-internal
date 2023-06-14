

package com.ark.bcp.domain.infrservice;

import java.util.Map;

/**
 */
public interface PrivateStorageService {
    /**
     * redis set
     *
     * @param key
     * @param value
     * @param ttlBySecend
     */
    void set(String key, String value, int ttlBySecend);

    /**
     * redis get
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * redis hget
     *
     * @param key
     * @param subkey
     * @return
     */
    String hget(String key, String subkey);

    /**
     * redis hmget
     *
     * @param key
     * @param kvs
     * @param ttlBySecend
     */
    void hmset(String key, Map<String, String> kvs, int ttlBySecend);
}
