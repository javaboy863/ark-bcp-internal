

package com.ark.bcp.domain.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 */
@Data
public class BaseDynamicScriptVO {
    private String scriptId;

    private String rawScriptSource;

    public static BaseDynamicScriptVO newInstance(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            return JSON.parseObject(str, BaseDynamicScriptVO.class);
        } catch (Exception e) {
            return null;
        }
    }
}
