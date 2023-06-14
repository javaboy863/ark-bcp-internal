package com.ark.bcp.domain.vo.cron;

/**
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum CronStrategyVO {
    EVERY_ONE(1, "每个..."),
    RANGE(2, "周期，从X-Y..."),
    REPEAT(3, "从X开始，每Y执行一次"),
    ENUMABLE(4, "指定");


    CronStrategyVO(Integer strategy, String name) {
        this.strategy = strategy;
        this.name = name;
    }

    private Integer strategy;
    private String name;

    public Integer getStrategy() {
        return strategy;
    }

    public String getName() {
        return name;
    }

    public static CronStrategyVO transFromStrategy(final Integer strategy) {
        for (CronStrategyVO value : values()) {
            if (value.getStrategy().equals(strategy)) {
                return value;
            }
        }
        return null;
    }
}
