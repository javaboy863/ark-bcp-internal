package com.ark.bcp.domain.service;

import com.ark.bcp.domain.entity.BcpAlertConfigEntity;
import com.ark.bcp.domain.entity.BcpCheckRuleAlertEntity;
import com.ark.bcp.domain.infrservice.AlertMessageService;
import com.ark.bcp.domain.vo.AlertMessageValueObject;
import com.ark.bcp.domain.alert.AlertFrequencyLimit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 */
@Service
@Slf4j
public class AlertMessageDomainService {

    @Resource
    private AlertMessageService alertMessageService;
    @Resource
    private ApplicationContext applicationContext;

    private AlertFrequencyLimit getAlertFrequencyLimit(final String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        Map<String, AlertFrequencyLimit> limitMap = applicationContext.getBeansOfType(AlertFrequencyLimit.class);
        if (CollectionUtils.isEmpty(limitMap)) {
            return null;
        }
        return limitMap.get(name);
    }

    public void send(AlertMessageValueObject message, BcpCheckRuleAlertEntity alertEntity) {
        log.info("alert message:{}", message);
        // 发送报警
        Runnable main = () -> {
            alertMessageService.sendMessage(message, alertEntity);
        };
        if (null == alertEntity) {
            log.info("未找到规则信息");
            main.run();
            return;
        }
        // 检查是否有报警流控
        AlertFrequencyLimit limiter = getAlertFrequencyLimit("default");
        if (null == limiter) {
            log.info("未配置limiter");
            main.run();
            return;
        }
        // 顺次执行
        synchronized (this) {
            BcpAlertConfigEntity entity = BcpAlertConfigEntity.builder().bindRuleId(String.valueOf(alertEntity.getRuleId())).build();
            if (limiter.isLimited(entity)) {
                return;
            }
            main.run();
            // 增加报警计数
            limiter.addTick(entity);
        }
    }

}
