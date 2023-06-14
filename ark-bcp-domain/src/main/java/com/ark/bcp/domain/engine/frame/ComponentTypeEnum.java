

package com.ark.bcp.domain.engine.frame;

/**
 */
@SuppressWarnings({"JavadocVariable", "AlibabaEnumConstantsMustHaveComment"})
public enum ComponentTypeEnum {
    EVENT("event", null),
    STRATEGYSET("strategyset", null),
    STRATEGY("strategy", null),
    RULE("rule", null),
    CONDITION("condition", null),
    CONDITIONSET("condition_set", null),
    TEMPLETE("templete", null),
    COUNTER("counter", null),
    DATASTREAM("data_stream", null),
    FEILD("feild", null),
    TRDFIELD("trd_field", null),
    CONSTANT_FIELD("constant_field", null),
    DEVICEINFO_FIELD("deviceinfo_field", null);

    ComponentTypeEnum(String type, Class clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    private String type;
    private Class clazz;

    public String getType() {
        return type;
    }

    public Class getClazz() {
        return clazz;
    }

    /**
     * 根据type获得枚举对象.
     *
     * @param type "
     * @return "
     */
    public static ComponentTypeEnum fromType(String type) {
        for (ComponentTypeEnum value : ComponentTypeEnum.values()) {
            if (value.getType().equalsIgnoreCase(type)) {
                return value;
            }
        }
        return null;
    }
}


