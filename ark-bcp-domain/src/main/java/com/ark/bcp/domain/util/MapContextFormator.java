

package com.ark.bcp.domain.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class MapContextFormator {
    private static Pattern PATTERN = Pattern.compile("(@[a-z0-9A-Z\\.\\-_]*@)");

    public static String formatRealTempleteKey(final String templeteName, final Map<String, Object> mapData) {
        if (StringUtils.isEmpty(templeteName) || CollectionUtils.isEmpty(mapData)) {
            return null;
        }
        String newTemplete = templeteName;
        Matcher matcher = PATTERN.matcher(newTemplete);
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String pathStr = placeholder.replace("@", "");
            String value = MapPathUtil.parse(pathStr, mapData);
            newTemplete = newTemplete.replace(placeholder, null == value ? "" : value);
            matcher = PATTERN.matcher(newTemplete);
        }
        return newTemplete;
    }

    public static String simplifyMatchTemplate(final String templateName) {
        if (StringUtils.isEmpty(templateName)) {
            return null;
        }
        String newTemplete = templateName;
        Matcher matcher = PATTERN.matcher(newTemplete);
        int index = 0;
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            newTemplete = newTemplete.replace(placeholder, "$" + index++ + "$");
            matcher = PATTERN.matcher(newTemplete);
        }
        return newTemplete;
    }

    public static boolean isHasPlaceHolder(final String templateName) {
        if (StringUtils.isEmpty(templateName)) {
            return false;
        }
        Matcher matcher = PATTERN.matcher(templateName);
        if (!matcher.find()) {
            return false;
        }
        return true;
    }

}
