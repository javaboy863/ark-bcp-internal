

package com.ark.bcp.domain.alert.impl;

import com.ark.bcp.domain.alert.AlertFrequencyLimit;
import com.ark.bcp.domain.infrservice.SimpleCounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 */
public abstract class AbstractAlertFrequencyLimit implements AlertFrequencyLimit, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AlertFrequencyLimitDefaultImpl.class);

    @Resource
    private SimpleCounterService simpleCounterService;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public SimpleCounterService getSimpleCounterApi() {
        return simpleCounterService;
    }
}
