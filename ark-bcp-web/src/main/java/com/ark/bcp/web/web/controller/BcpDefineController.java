
package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 */
@Controller
@RequestMapping({BcpDefineController.URLPATH})
public class BcpDefineController {

    /**
     * 访问路径.
     */
    public static final String URLPATH = "/risk/bcp/bg/def";

    /**
     * 获取接入类型汇总.
     */
    @ResponseBody
    @RequestMapping(value = "/accesstype", method = RequestMethod.GET)
    public Result<JSONArray> invokeAccessType() {
        return ResultUtils.wrapSuccess(buildJsonArray());
    }

    /**
     * 构建json array
     */
    private JSONArray buildJsonArray() {
        JSONArray accessTypes = new JSONArray();
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", "name1");
        itemObject.put("code", "code1");
        accessTypes.add(itemObject);
        return accessTypes;
    }


}

