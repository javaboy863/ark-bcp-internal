
package com.ark.bcp.app.utils;

import com.missfresh.risk.bcp.domain.vo.LoadDataGroovyScriptVO;
import com.missfresh.risk.bcp.dto.LoadDataGroovyScriptDto;

/**
 */
public class LoadDataGroovyScripts {
    public static LoadDataGroovyScriptDto toLoadDataGroovyScriptDto(final LoadDataGroovyScriptVO loadDataGroovyScriptVO) {
        if (null == loadDataGroovyScriptVO) {
            return null;
        }
        return LoadDataGroovyScriptDto.builder()
                .loadDataScriptId(loadDataGroovyScriptVO.getDynamicCodeConfigId())
                .loadDataScriptSrc(loadDataGroovyScriptVO.getDynamicCodeSrc())
                .dispatchStrategy(loadDataGroovyScriptVO.getDispatchStrategy())
                .build();
    }

    public static LoadDataGroovyScriptVO fromLoadDataGroovyScriptDto(final LoadDataGroovyScriptDto loadDataGroovyScriptDto) {
        if (null == loadDataGroovyScriptDto) {
            return null;
        }
        return LoadDataGroovyScriptVO.builder()
                .dispatchStrategy(loadDataGroovyScriptDto.getDispatchStrategy())
                .dynamicCodeConfigId(loadDataGroovyScriptDto.getLoadDataScriptId())
                .dynamicCodeSrc(loadDataGroovyScriptDto.getLoadDataScriptSrc())
                .build();
    }
}
