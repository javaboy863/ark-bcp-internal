

package com.ark.bcp.infr.support;

import com.missfresh.risk.bcp.domain.infrservice.UserInfoService;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;

/**
 */
public class UserInfoUtil {


    public static String usetRegistPhone(Long userid) {
        if (null == userid) {
            return null;
        }
        UserInfoService userInfoService = AbstractApplicationContextUtil.getExtension(UserInfoService.class, "UserInfoService");
        if (null != userInfoService) {
            return userInfoService.userphone(userid);
        }
        return null;
    }
}
