
package com.ark.bcp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoadDataGroovyScriptVO {
    private transient Long dynamicCodeConfigId;
    /**
     * @see com.missfresh.risk.bcp.enums.DispatchStrategyDefine
     */
    private Integer dispatchStrategy;

    private transient String dynamicCodeSrc;
}
