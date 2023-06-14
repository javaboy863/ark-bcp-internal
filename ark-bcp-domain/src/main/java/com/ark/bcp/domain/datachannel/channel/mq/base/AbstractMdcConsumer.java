
package com.ark.bcp.domain.datachannel.channel.mq.base;

import com.google.common.base.Stopwatch;
import com.missfresh.as.log.Logger;
import com.missfresh.as.log.LoggerFactory;
import com.missfresh.as.log.container.ThreadLocalContainer;
import com.missfresh.as.log.enums.SourceTagEnum;
import com.missfresh.as.log.utils.TimeUtil;
import org.slf4j.MDC;

/**
 */
public abstract class AbstractMdcConsumer extends AbstractDataChannel {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMdcConsumer.class);

    private String appcode = "risk-bcp";

    /**
     * 处理json类型消息.
     *
     * @param object ""
     * @return ""
     */
    public abstract boolean processMdc(Object object);

    /**
     * 获取日志url.
     *
     * @return ""
     */
    public abstract String getUrl();


    final public boolean process(Object object) {
        String url = getUrl();

        ThreadLocalContainer.setURI(url);
        String logId = ThreadLocalContainer.getUinqueLogId();

        MDC.put("logid", "logid[" + logId + "]");
        MDC.put("url", "url[" + url + "]");

        //调用filter链
        TimeUtil.start(SourceTagEnum.TOTAL, "");
        Stopwatch stopwatch = Stopwatch.createStarted();
        boolean result = processMdc(object);
        TimeUtil.end(SourceTagEnum.TOTAL, "");

        return result;
    }
}

