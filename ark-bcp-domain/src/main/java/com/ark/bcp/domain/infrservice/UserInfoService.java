package com.ark.bcp.domain.infrservice;

import com.ark.bcp.domain.vo.bizzvos.BizzEnumVO;

/**
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public interface UserInfoService {
    boolean isNewUser(Long userId);

    boolean isNewUserV2(Long userid, BizzEnumVO bizzEnumVO);

    String userphone(Long userid);
}
