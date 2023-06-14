package com.ark.bcp.domain.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.util.MapPathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
@Data
@AllArgsConstructor
public class AlertMessageValueObject implements Serializable {
    private static final long serialVersionUID = -7131921108973455693L;

    private static Pattern PATTERN = Pattern.compile("(@[a-z0-9A-Z\\.\\-_]*@)");

    private String message;
    private String state = "alerting";
    private String ruleName;
    private String ruleUrl = "";
    private int alertLevel = 0;

    private AlertMessageValueObject(String message, String ruleName) {
        this.message = message;
        this.ruleName = ruleName;
    }

    public static AlertMessageValueObject transAlertMsg(
            final String textFormat, final String rawContext, final String title) {
        JSONObject mapData = JSON.parseObject(rawContext);
        // 默认格式，输出原始数据
        if (StringUtils.isEmpty(textFormat) || null == mapData) {
            String reason = "告警测试: " + rawContext;
            return new AlertMessageValueObject(reason, title);
        }
        String newContext = textFormat;
        Matcher matcher = PATTERN.matcher(textFormat);
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String pathStr = placeholder.replace("@", "");
            String value = MapPathUtil.parse(pathStr, mapData);
            newContext = newContext.replace(placeholder, null == value ? "" : value);
            matcher = PATTERN.matcher(newContext);
        }
        return new AlertMessageValueObject(newContext, title);
    }
}
