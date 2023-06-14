
package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.mryx.grampus.ccs.dto.CcsLoginUser;
import com.mryx.grampus.ccs.rpc.CcsUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 **/
@RestController
@RequestMapping({"/risk/bcp/recommend", "/risk/bcp/bg/recommend"})
public class CcsApiController  {
    /**
     * ccs密钥
     */
    @Value("${ccs.ess.secret}")
    private String fkSecret;

    /**
     * ccs应用id
     */
    @Value("${ccs.app.id}")
    private String fkAppId;

    /**
     * 统一账号认证服务
     */
    @Resource
    private CcsUserService ccsUserService;


    /**
     * 获取当前登录用户
     */
    @RequestMapping({"/login"})
    public Result<JSONObject> login(HttpServletRequest request) {
        //拿到当前登录用户
        CcsLoginUser user = getUser();
        //构建并返回
        return ResultUtils.wrapSuccess(buildJsonObject(user));
    }

    /**
     * 返回当前用户有权限登录的应用列表
     */
    @RequestMapping({"/formDesigner/user/queryUserOauthApp"})
    public Result<Object> queryUserOauthApp(HttpServletRequest request) {
        //拿到当前登录用户
        CcsLoginUser user = getUser();
        //获取用户有权限登录的应用列表
        return ResultUtils.wrapSuccess(ccsUserService.getAppList(user.getOauthId()));
    }


    /**
     * 获取用户在应用里的菜单
     */
    @RequestMapping("/menu")
    public Result menu(HttpServletRequest request) {
        //拿到当前登录用户
        CcsLoginUser user = getUser();
        //获取用户在应用里的菜单
        return ResultUtils.wrapSuccess(ccsUserService.getMenuByOauthId(Integer.valueOf(fkAppId), fkSecret, user.getOauthId(), System.currentTimeMillis()));
    }


    /**
     * 拿到当前登录用户
     */
    private CcsLoginUser getUser() {
        return CcsLoginUser.get();
    }

    /**
     * 构建ccs json对象
     */
    private JSONObject buildJsonObject(CcsLoginUser user) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", user.getOauthName());
        jsonObject.put("id", user.getOauthId());
        return jsonObject;
    }
}
