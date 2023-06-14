

package com.ark.bcp.infr.alert;

import com.alibaba.fastjson.JSON;
import com.ark.bcp.infr.channel.ILarkAlertService;
import com.missfresh.risk.bcp.domain.entity.BcpCheckRuleAlertEntity;
import com.missfresh.risk.bcp.domain.exception.FailfastException;
import com.missfresh.risk.bcp.domain.exception.IllegalParamException;
import com.missfresh.risk.bcp.domain.infrservice.AlertMessageService;
import com.missfresh.risk.bcp.domain.vo.AlertChannelParams;
import com.missfresh.risk.bcp.domain.vo.AlertMessageValueObject;
import com.missfresh.risk.bcp.enums.AlertChannelDefine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 */
@Service
public class AlertServiceImpl implements AlertMessageService {
    private static Logger logger = LoggerFactory.getLogger(AlertServiceImpl.class);

    /**
     * lark.
     */
    @Resource
    private ILarkAlertService larkAlertService;

    @Override
    public void sendMessage(AlertMessageValueObject alertValueObject, BcpCheckRuleAlertEntity alertEntity) {
        try {
            sendMessageCheckParams(alertValueObject, alertEntity);
            sendMessageByChannel(alertValueObject, alertEntity);
        } catch (IllegalParamException illegalParamException) {
            logger.info("参数异常:{},{}", JSON.toJSONString(alertValueObject), JSON.toJSONString(alertEntity));
            throw illegalParamException;
        } catch (FailfastException failfastException) {
            logger.info("发送报警错误:{}->{}", failfastException.getMessage(), JSON.toJSONString(alertEntity));
            throw failfastException;
        } catch (Exception e) {
            logger.error("未知异常:{}",JSON.toJSONString(alertEntity), e);
            throw e;
        }
    }

    private void sendMessageCheckParams(AlertMessageValueObject alertValueObject, BcpCheckRuleAlertEntity alertEntity) {
        if (null == alertValueObject || null == alertEntity) {
            throw new IllegalParamException("参数异常");
        }
    }

    private void sendMessageByChannel(AlertMessageValueObject alertValueObject, BcpCheckRuleAlertEntity alertEntity) {
        final AlertChannelDefine alertChannelDefine = AlertChannelDefine.fromCode(alertEntity.getAlertType());
        do {
            if (AlertChannelDefine.ALERT_CENTER == alertChannelDefine && !StringUtils.isEmpty(alertEntity.getAppCode())) {
                larkAlertService.alertByAppcode(alertEntity.getAppCode(), JSON.toJSONString(alertValueObject));
                break;
            }
            if (AlertChannelDefine.ALERT_LARK_GROUP == alertChannelDefine) {
                AlertChannelParams params = JSON.parseObject(alertEntity.getAlertConfigJson(), AlertChannelParams.class);
                if (null != params) {
                    larkAlertService.alert(params.getAlertUrl(), alertValueObject.getRuleName(), alertValueObject.getMessage());
                    break;
                }
            }
            throw new FailfastException(null, "不识别的报警渠道");
        } while (false);
    }
}
