

package com.ark.bcp.domain.datachannel.channel.mq.base;

import com.ark.bcp.domain.datachannel.channel.DataChannel;
import com.ark.bcp.domain.util.ConfigLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 */
public abstract class AbstractDataChannel implements DataChannel {
    private static final Logger log = LoggerFactory.getLogger(AbstractDataChannel.class);

    protected static Boolean INIT_CONSUMER = false;

    static {
        try {
            Properties properties = ConfigLoaderUtils.loadConfig("application.properties");
            String startOnInit = properties.getProperty("init.consumer", "false");
            INIT_CONSUMER = Boolean.parseBoolean(startOnInit);
        } catch (Exception e) {
            log.error("read bcp.properties error", e);
            INIT_CONSUMER = false;
        }
        log.info("initConsumer value:{}", INIT_CONSUMER);
    }

    private String configParam;


    /**
     * 初始化通道
     *
     * @param params
     * @return
     */
    @Override
    public boolean init(String params) {
        configParam = params;
        return false;
    }

    /**
     * 启动管道
     */
    @Override
    public void start() {
        if (INIT_CONSUMER) {
            log.info("启动事件源的MQ consumer，:{}", configParam);
            safeStart();
        } else {
            log.info("不不不不启动事件源的MQ consumer:{}", configParam);
        }
    }

    /**
     * 关闭管道
     */
    @Override
    public void stop() {
        log.info("事件源停止:{}", configParam);
        safeStop();
    }

    /**
     * 安全启动
     */
    public abstract void safeStart();

    /**
     * 安全停止
     */
    public abstract void safeStop();
}
