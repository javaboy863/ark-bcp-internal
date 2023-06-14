

package com.ark.bcp.infr.proxy;

import com.missfresh.risk.bcp.domain.infrservice.ExpressExecutorService;
import com.missfresh.risk.bcp.domain.util.Namespace;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.config.QLExpressRunStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 */
@Service
public class QlExpressExecutorServiceImpl implements InitializingBean, ExpressExecutorService {
    private Logger logger = LoggerFactory.getLogger(QlExpressExecutorServiceImpl.class);

    @Resource
    private UserInfoServiceProxyImpl userInfoServiceProxy;


    @Resource
    private VoucherTemplateServiceProxyImpl voucherTemplateServiceProxy;


    private volatile ExpressRunner runner;
    private static DefaultContext<String, Object> context = new DefaultContext<>();

    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    @SuppressWarnings({"LineLength"})
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            ExpressRunner tmpRuner = new ExpressRunner();
            tmpRuner.addFunctionOfClassMethod(
                    "是否为空", this.getClass().getName(), "isEmpty", new String[]{"String"}, null);
            QLExpressRunStrategy.setForbiddenInvokeSecurityRiskMethods(true);
            addLocalFunction(tmpRuner, "isNewUserFun", userInfoServiceProxy, "isNewUser", new Class[]{Long.class});
            addLocalFunction(tmpRuner, "isNewUserVoucherFun", voucherTemplateServiceProxy, "isNewUserVoucher", new Class[]{Long.class});
            addLocalFunction(tmpRuner, "isNewUserVoucherCodeFun", voucherTemplateServiceProxy, "isNewUserVoucherCode", new Class[]{String.class});
            runner = tmpRuner;
        } catch (Exception e) {
            logger.error(Namespace.ENGINE_CONFIG_LOG_PREFIX + "QLExpressExcuter init exception:", e);
        }
    }

    @Override
    public Object execute(String script) throws Exception {
        return execute(script, null);
    }

    @Override
    public Object execute(String script, Object conntextParams) throws Exception {
        if (StringUtils.isEmpty(script)) {
            return null;
        }
        if (null == runner) {
            logger.error(Namespace.ENGINE_CONFIG_LOG_PREFIX + "QLExpressExcuter runner not is null!!");
            return null;
        }

        DefaultContext<String, Object> defaultContext = transtoContext(conntextParams);
        return runner.execute(script, defaultContext, null, false, false, 1000);
    }

    private DefaultContext<String, Object> transtoContext(Object object) {
        if (null == object) {
            return context;
        }
        DefaultContext<String, Object> retMap = new DefaultContext<String, Object>();
        if (object instanceof Map) {
            Set<Map.Entry> entrySet = ((Map) object).entrySet();
            for (Map.Entry entry : entrySet) {
                if (entry.getKey() instanceof String) {
                    retMap.put((String) entry.getKey(), entry.getValue());
                }
            }
        }
        return retMap;
    }


    /**
     * 增加本地方法
     *
     * @param runner
     * @param name
     * @param aServiceObject
     * @param aFunctionName
     * @param aParameterClassTypes
     */
    public void addLocalFunction(
            ExpressRunner runner,
            String name,
            Object aServiceObject,
            String aFunctionName,
            Class<?>[] aParameterClassTypes) {
        logger.info("=================initRunner Start===============");
        try {
            runner.addFunctionOfServiceMethod(name, aServiceObject, aFunctionName, aParameterClassTypes, null);
        } catch (Exception e) {
            logger.error("初始化表达式失败:", e);
            throw new RuntimeException("初始化失败表达式", e);
        }
        logger.info("=================initRunner End===============");
    }

}
