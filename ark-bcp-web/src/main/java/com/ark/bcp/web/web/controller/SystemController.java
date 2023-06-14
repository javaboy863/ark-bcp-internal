package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.missfresh.risk.bcp.domain.util.AbstractApplicationContextUtil;
import com.missfresh.risk.bcp.domain.util.ConfigLoaderUtils;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

/**
 **/
@RestController
public class SystemController implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Value("${zkaddrs}")
    private String zkaddrs;

    /**
     * 健康检查
     */
    @RequestMapping(value = "/healthcheck", method = RequestMethod.GET)
    public String healthcheck() {
        logger.info("healthcheck");
        return "success";
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Properties properties = ConfigLoaderUtils.loadConfig("groovyEnv.properties");
        //是否加载测试脚本
        if (isLoadTestScript(properties)){
            return;
        }
        //加载zk到setProperty
        setProperty(properties);
        //加载DynamicDubboInvoke脚本
        loadClass(properties);
    }

    /**
     * 是否加载测试脚本
     */
    private boolean isLoadTestScript(Properties properties) {
        if (null != properties) {
            return false;
        }
        //加载测试脚本
        loadTestScript();
        return true;
    }

    private void setProperty(Properties properties) {
        logger.info("groovy api initEnv : {}", properties);
        // 必须搞这么一下，否则k8s 过不去 TT...
        logger.info("replace zkaddrs:{}", zkaddrs);
        properties.setProperty("zkaddrs", zkaddrs);
    }

    /**
     * 初始化 GroovyClassLoader
     */
    private  GroovyClassLoader initGroovyClassLoader() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        // 设置该GroovyClassLoader的父ClassLoader为当前线程的加载器(默认)
        return new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
    }

    /**
     * 加载DynamicDubboInvoke 脚本
     */
    private void loadClass(Properties properties) {
        try {
            Class<?> groovyClass = initGroovyClassLoader().loadClass("com.missfresh.risk.bcp.web.script.DynamicDubboInvoke");
            // 获得TestGroovy的实例
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            groovyObject.invokeMethod("initEnv", new Object[]{properties});
            logger.info("groovy api initEnv : {}", properties);
        } catch (Exception e) {
            logger.error("load class error:", e);
        }
    }

    /**
     * 读取groovy测试脚本
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    private void loadTestScript() {
        try {
            Class<?> groovyClass = initGroovyClassLoader().loadClass("com.missfresh.risk.bcp.web.script.OrderAlertRedAlertJDCheck");
            // 获得TestGroovy的实例
            JSONObject jsonObject = JSON.parseObject("{\n" +
                    "    \"orderItem\": {\n" +
                    "        \"added\": false,\n" +
                    "        \"createdTime\": 1608781182000,\n" +
                    "        \"extJson\": {\n" +
                    "            \"supplierId\": \"\",\n" +
                    "            \"processLineId\": 0,\n" +
                    "            \"promotionPrice\": 990,\n" +
                    "            \"unitNum\": 2,\n" +
                    "            \"itemGrade\": 3,\n" +
                    "            \"marketTag\": 0,\n" +
                    "            \"store\": \"{\\\"stationCode\\\":\\\"MRYX|mryx_lgy1\\\",\\\"storeHouseId\\\":\\\"MRYXBJS\\\",\\\"storeId\\\":\\\"MRYXTJ-LAIGUANGYING\\\",\\\"storeOpenEndTime\\\":\\\"20:00\\\",\\\"storeOpenType\\\":\\\"NORMAL\\\",\\\"warehouseGroupVer\\\":0}\",\n" +
                    "            \"isDiscount\": true,\n" +
                    "            \"capacityRange\": \"{\\\"dayTime\\\":\\\"2020-12-24 11:39:41\\\",\\\"rangeId\\\":0}\",\n" +
                    "            \"settleOriginPrice\": 990,\n" +
                    "            \"priceList\": [\n" +
                    "                990\n" +
                    "            ],\n" +
                    "            \"businessNo\": 0,\n" +
                    "            \"sceneType\": \"10\",\n" +
                    "            \"userPrice\": 990,\n" +
                    "            \"isFreeShip\": false,\n" +
                    "            \"forceCheckStock\": true,\n" +
                    "            \"saleGroupType\": \"1\",\n" +
                    "            \"afterSaleType\": \"00111\"\n" +
                    "        },\n" +
                    "        \"fullPromotionAmount\": 0,\n" +
                    "        \"hasNightFee\": false,\n" +
                    "        \"id\": 2234859,\n" +
                    "        \"orderId\": \"2012241139419303624\",\n" +
                    "        \"orderItemId\": \"20122411394193036241\",\n" +
                    "        \"originFee\": 990,\n" +
                    "        \"payFee\": 990,\n" +
                    "        \"price\": 990,\n" +
                    "        \"priceList\": [\n" +
                    "            990\n" +
                    "        ],\n" +
                    "        \"productId\": \"10336\",\n" +
                    "        \"productImg\": \"\",\n" +
                    "        \"productName\": \"新希望airsnow轻爱原味酸奶200g*2\",\n" +
                    "        \"productType\": 1,\n" +
                    "        \"promotionPrice\": 990,\n" +
                    "        \"pssOwner\": \"MRYX\",\n" +
                    "        \"purchasePrice\": -1,\n" +
                    "        \"quantity\": 1,\n" +
                    "        \"saleGroupType\": \"1\",\n" +
                    "        \"saleModel\": 1,\n" +
                    "        \"salePlatfrom\": 1,\n" +
                    "        \"settleOriginPrice\": 990,\n" +
                    "        \"singlePromotionAmount\": 0,\n" +
                    "        \"sku\": \"p-hn-xxwqaywsn-2h\",\n" +
                    "        \"unitNum\": 2,\n" +
                    "        \"unitType\": \"盒\",\n" +
                    "        \"updatedTime\": 1608781182000,\n" +
                    "        \"userPrice\": 990\n" +
                    "    },\n" +
                    "    \"orderFeeSplits\": {\n" +
                    "        \"6\": {\n" +
                    "            \"accountType\": 255,\n" +
                    "            \"createdTime\": 1608781182000,\n" +
                    "            \"fee\": 990,\n" +
                    "            \"feeType\": 3,\n" +
                    "            \"id\": 184519,\n" +
                    "            \"orderId\": \"2012241139419303624\",\n" +
                    "            \"orderItemId\": \"20122411394193036241\",\n" +
                    "            \"price\": 100,\n" +
                    "            \"productId\": \"10336\",\n" +
                    "            \"productType\": 0,\n" +
                    "            \"quantity\": 1,\n" +
                    "            \"sku\": \"p-hn-xxwqaywsn-2h\",\n" +
                    "            \"updatedTime\": 1608781182000\n" +
                    "        },\n" +
                    "        \"4\": {\n" +
                    "            \"accountType\": 3,\n" +
                    "            \"createdTime\": 1608781182000,\n" +
                    "            \"fee\": 10,\n" +
                    "            \"feeType\": 4,\n" +
                    "            \"id\": 184520,\n" +
                    "            \"orderId\": \"2012241139419303624\",\n" +
                    "            \"orderItemId\": \"20122411394193036241\",\n" +
                    "            \"price\": 990,\n" +
                    "            \"productId\": \"10336\",\n" +
                    "            \"productType\": 0,\n" +
                    "            \"quantity\": 1,\n" +
                    "            \"sku\": \"p-hn-xxwqaywsn-2h\",\n" +
                    "            \"updatedTime\": 1608781182000\n" +
                    "        },\n" +
                    "        \"11\": {\n" +
                    "            \"accountType\": 255,\n" +
                    "            \"createdTime\": 1608781182000,\n" +
                    "            \"fee\": 500,\n" +
                    "            \"feeType\": 11,\n" +
                    "            \"id\": 184517,\n" +
                    "            \"orderId\": \"2012241139419303624\",\n" +
                    "            \"orderItemId\": \"20122411394193036241\",\n" +
                    "            \"price\": 990,\n" +
                    "            \"productId\": \"10336\",\n" +
                    "            \"productType\": 0,\n" +
                    "            \"quantity\": 1,\n" +
                    "            \"sku\": \"p-hn-xxwqaywsn-2h\",\n" +
                    "            \"updatedTime\": 1608781182000\n" +
                    "        },\n" +
                    "        \"12\": {\n" +
                    "            \"accountType\": 3,\n" +
                    "            \"createdTime\": 1608781182000,\n" +
                    "            \"fee\": 5,\n" +
                    "            \"feeType\": 12,\n" +
                    "            \"id\": 184516,\n" +
                    "            \"orderId\": \"2012241139419303624\",\n" +
                    "            \"orderItemId\": \"20122411394193036241\",\n" +
                    "            \"price\": 990,\n" +
                    "            \"productId\": \"10336\",\n" +
                    "            \"productType\": 0,\n" +
                    "            \"quantity\": 1,\n" +
                    "            \"sku\": \"p-hn-xxwqaywsn-2h\",\n" +
                    "            \"updatedTime\": 1608781182000\n" +
                    "        },\n" +
                    "        \"82\": {\n" +
                    "            \"accountType\": 255,\n" +
                    "            \"createdTime\": 1608781182000,\n" +
                    "            \"fee\": 500,\n" +
                    "            \"feeType\": 82,\n" +
                    "            \"id\": 184518,\n" +
                    "            \"orderId\": \"2012241139419303624\",\n" +
                    "            \"orderItemId\": \"20122411394193036241\",\n" +
                    "            \"price\": 990,\n" +
                    "            \"productId\": \"10336\",\n" +
                    "            \"productType\": 0,\n" +
                    "            \"quantity\": 1,\n" +
                    "            \"sku\": \"p-hn-xxwqaywsn-2h\",\n" +
                    "            \"updatedTime\": 1608781182000\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"order\": {\n" +
                    "        \"activeSource\": \"\",\n" +
                    "        \"activityType\": 0,\n" +
                    "        \"binflag\": 0,\n" +
                    "        \"bizOrderId\": \"2071242\",\n" +
                    "        \"bizSource\": 10,\n" +
                    "        \"createdTime\": 1608781182000,\n" +
                    "        \"evaluated\": false,\n" +
                    "        \"expireTime\": 1608782081000,\n" +
                    "        \"extJson\": {\n" +
                    "            \"businessNo\": \"0\",\n" +
                    "            \"freshCoinCount\": 0,\n" +
                    "            \"dailyReceiveDeadLineTime\": \"2020-12-24 14:33:00\",\n" +
                    "            \"freshCoinAmount\": 0,\n" +
                    "            \"productVoucherSave\": 0,\n" +
                    "            \"clientAiSwitch\": \"ON\",\n" +
                    "            \"vip\": false,\n" +
                    "            \"isNewUser\": false,\n" +
                    "            \"capacityRange\": {\n" +
                    "                \"dayTime\": 1608781181041,\n" +
                    "                \"rangeId\": 0\n" +
                    "            },\n" +
                    "            \"activeSource\": \"\"\n" +
                    "        },\n" +
                    "        \"freshCoinAmount\": 0,\n" +
                    "        \"freshCoinCount\": 0,\n" +
                    "        \"groupId\": \"\",\n" +
                    "        \"groupNum\": 0,\n" +
                    "        \"groupOrderId\": \"\",\n" +
                    "        \"id\": 341855,\n" +
                    "        \"orderCancelType\": 10,\n" +
                    "        \"orderCanceledTime\": 1608781524000,\n" +
                    "        \"orderId\": \"2012241139419303624\",\n" +
                    "        \"orderStatus\": 90,\n" +
                    "        \"orderType\": 10,\n" +
                    "        \"parentOrderId\": \"\",\n" +
                    "        \"payFee\": 1490,\n" +
                    "        \"payTypeEnumCode\": 7,\n" +
                    "        \"popBusinessNo\": \"0\",\n" +
                    "        \"pssOwner\": \"MRYX\",\n" +
                    "        \"saleModel\": 1,\n" +
                    "        \"shopId\": \"\",\n" +
                    "        \"shopSource\": 0,\n" +
                    "        \"splitTypeEnumCode\": 0,\n" +
                    "        \"storeRemark\": \"\",\n" +
                    "        \"totalFee\": 1490,\n" +
                    "        \"updatedTime\": 1608781524000,\n" +
                    "        \"userId\": \"74941624\",\n" +
                    "        \"userRemark\": \"{\\\"tags\\\":[{\\\"id\\\":3,\\\"saleGroupType\\\":1,\\\"tagCategory\\\":\\\"SHORTAGE\\\",\\\"userMsg\\\":\\\"缺货时电话与我沟通\\\"}]}\"\n" +
                    "    }\n" +
                    "}");

            jsonObject.getJSONObject("order").put("createdTime", System.currentTimeMillis());
            jsonObject.getJSONObject("order").put("popBusinessNo", "42314");
            jsonObject.getJSONObject("order").put("orderType", "80");

            jsonObject.getJSONObject("orderItem").put("quantity", 51);
            jsonObject.getJSONObject("orderItem").put("payFee", 80);
            jsonObject.getJSONObject("orderItem").put("purchasePrice", 100);

            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            Object result = groovyObject.invokeMethod("handle", new Object[]{jsonObject});
            logger.info("groovy api initEnv : {}", jsonObject);
        } catch (Exception e) {
            logger.error("load class error:", e);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AbstractApplicationContextUtil.addApplicationContext(applicationContext);
    }
}
