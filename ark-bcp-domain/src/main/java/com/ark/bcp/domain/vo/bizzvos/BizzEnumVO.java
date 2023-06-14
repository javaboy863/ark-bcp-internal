
package com.ark.bcp.domain.vo.bizzvos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 各个平台码的值对象
 * @author wangzheng@missfresh.cn on 2020/11/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BizzEnumVO {
    private Integer bizzEnumType;
    private String bizzEnumValue;

    public void setBizzEnumValue(Integer code) {
        bizzEnumValue = String.valueOf(code);
    }
}
