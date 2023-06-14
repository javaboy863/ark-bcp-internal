package com.ark.bcp.infr.proxy;

import com.missfresh.risk.bcp.domain.infrservice.UserInfoService;
import com.missfresh.risk.bcp.domain.vo.bizzvos.BizzEnumType;
import com.missfresh.risk.bcp.domain.vo.bizzvos.BizzEnumVO;
import com.missfresh.risk.bcp.domain.vo.bizzvos.BizzEnumVoUtils;
import com.missfresh.user.bean.UserInfo;
import com.missfresh.user.bean.UserQueryBean;
import com.missfresh.user.bean.enums.BusinessLineEnum;
import com.missfresh.user.service.IUserCenterService;
import com.mryx.ark.sdk.enums.BizzEnums;
import com.mryx.ark.tos.api.IUserOrderStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Service("userInfoService")
@Slf4j
public class UserInfoServiceProxyImpl implements UserInfoService {

    @Resource
    private IUserOrderStatusService userOrderStatusService;

    @Resource
    private IUserCenterService userCenterService;

    @Override
    public boolean isNewUser(Long userId) {
        log.debug("userId:{}, isNewUser:{}", userId, false);
        return isNewUserV2(userId, BizzEnumVO.builder().bizzEnumType(BizzEnumType.ARK.getCode())
                .bizzEnumValue(String.valueOf(BizzEnums.AS.getCode())).build());
    }

    @Override
    public boolean isNewUserV2(Long userId, BizzEnumVO bizzEnumVO) {
        log.debug("userId:{}, bizzEnumVo:{}", userId, bizzEnumVO);
        try {
            BizzEnums bizzEnums = BizzEnumVoUtils.transToArkBizzEnum(bizzEnumVO);
            boolean isUserOrder = !userOrderStatusService.isOrderPaiedUser("risk-bcp", userId, bizzEnums);
            log.debug("user order, id:{}, isUserOrder:{}", userId, isUserOrder);
            return isUserOrder;
        } catch (Exception e) {
            log.error("call is userOrder error:", e);
            throw e;
        }
    }

    @Override
    public String userphone(Long userid) {
        try {
            UserQueryBean userQueryBean = new UserQueryBean();
            userQueryBean.setNeedBase(true);
            userQueryBean.setBusinessLineEnum(BusinessLineEnum.MRYX);
            UserInfo userinfo = userCenterService.getUser(userid, userQueryBean);
            if (null != userinfo && null != userinfo.getUserBase()) {
                return userinfo.getUserBase().getMobile();
            }
            return null;
        } catch (Exception e) {
            log.error("获取用户信息异常:{}",userid, e);
        }
        return null;
    }
}
