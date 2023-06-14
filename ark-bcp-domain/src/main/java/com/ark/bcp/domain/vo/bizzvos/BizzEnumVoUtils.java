
package com.ark.bcp.domain.vo.bizzvos;


import com.mryx.ark.sdk.enums.BizzEnums;

/**
 * 各个平台码的值对象
 *
 * @author wangzheng@missfresh.cn on 2020/11/18
 */

public class BizzEnumVoUtils {
    public static BizzEnums transToArkBizzEnum(final BizzEnumVO bizzEnumVO) {
        if (null == bizzEnumVO) {
            return null;
        }
        if (BizzEnumType.ARK.getCode().equals(bizzEnumVO.getBizzEnumType())) {
            Integer code = Integer.valueOf(bizzEnumVO.getBizzEnumValue());
            if (BizzEnums.isInclude(code)) {
                return BizzEnums.valueOf(code);
            }
        }
        return null;
    }
}
