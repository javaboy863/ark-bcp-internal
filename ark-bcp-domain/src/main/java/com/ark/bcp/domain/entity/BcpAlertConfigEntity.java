

package com.ark.bcp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BcpAlertConfigEntity {

    /**
     * 关联的规则id.
     */
    private String bindRuleId;

}
