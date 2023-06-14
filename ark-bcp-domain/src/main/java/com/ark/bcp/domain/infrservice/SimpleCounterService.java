package com.ark.bcp.domain.infrservice;


import com.ark.bcp.domain.util.TimeUnitEnum;

import java.util.List;

/**
 */
public interface SimpleCounterService {

    /**
     * 个数添加.
     *
     * @param key      指标id+主属性
     * @param timeUnit 时间单位
     * @param interval 窗口大小
     * @param value    添加值.
     */
    void add(String key, String value, TimeUnitEnum timeUnit, long interval);

    /**
     * 个数添加.
     * @param key
     * @param member
     * @param offset
     * @param timeUnit
     * @param interval
     */
    void add(String key, String member, Long offset, TimeUnitEnum timeUnit, long interval);

    /**
     * 个数统计结果.
     *
     * @param key      指标id+主属性
     * @param timeUnit 时间单位
     * @param interval 窗口大小
     * @return 个数统计结果
     */
    long getWordCount(String key, TimeUnitEnum timeUnit, long interval);


    /**
     * 个数统计实际的各个值.
     *
     * @param key      指标id+主属性
     * @param timeUnit 时间单位
     * @param interval 窗口大小
     * @return 值得list
     */
    List<String> getWordCountValue(String key, TimeUnitEnum timeUnit, long interval);


    /**
     * 删除一个key.
     *
     * @param key ""
     */
    void delWordCount(String key);
}
