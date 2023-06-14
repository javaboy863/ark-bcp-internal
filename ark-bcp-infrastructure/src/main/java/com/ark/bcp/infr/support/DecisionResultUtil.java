

package com.ark.bcp.infr.support;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class DecisionResultUtil {
    public static Map<String, Object> wrapAlert(final List<Pair<String, Object>> alertContext) {
        Map<String, Object> retData = new HashMap<>(8);
        retData.put("hited", true);
        retData.put("msg", "");
        if (!CollectionUtils.isEmpty(alertContext)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Pair<String, Object> pair : alertContext) {
                stringBuilder.append(pair.getLeft()).append(": ").append(pair.getRight()).append("\n");
            }
            retData.put("msg", stringBuilder.toString());
        }
        return retData;
    }

    public static Map<String, Object> wrapNormal() {
        Map<String, Object> retData = new HashMap<>(8);
        retData.put("hited", false);
        retData.put("msg", "");
        return retData;
    }
}
