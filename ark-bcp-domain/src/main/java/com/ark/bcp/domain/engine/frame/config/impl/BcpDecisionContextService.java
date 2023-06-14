

package com.ark.bcp.domain.engine.frame.config.impl;

import com.ark.bcp.domain.engine.frame.decision.execute.Constant;
import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.engine.frame.decision.execute.DecisionExcuteResult;
import com.ark.bcp.domain.engine.frame.decision.execute.FieldSetterReader;
import com.ark.bcp.domain.engine.frame.decision.execute.IDecisionContext;
import com.ark.bcp.domain.engine.frame.decision.execute.event.AbstractExecutableBO;
import com.ark.bcp.domain.exception.UnknowExecutsetException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.missfresh.risk.bcp.domain.engine.frame.decision.execute.*;
import com.ark.bcp.domain.util.TimeTrace;
import com.mryx.monitor.api.BusinessMonitor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;


/**
 */
@SuppressWarnings({"LineLength"})
@Service
public class BcpDecisionContextService extends BcpEventFactory implements IDecisionContext {
    private static final Logger logger = LoggerFactory.getLogger(BcpDecisionContextService.class);

    private static ThreadPoolExecutor rulesExecutorService;

    private ScheduledExecutorService monitorExecutorService;


    static {
        rulesExecutorService = new ThreadPoolExecutor(
                200, 400, 600L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(5120),
                new ThreadFactoryBuilder().setNameFormat("ENGINE-RULE-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private void initThreadPool() {
        monitorExecutorService = Executors.newScheduledThreadPool(1);
        monitorExecutorService.scheduleAtFixedRate(() -> {
            BusinessMonitor.recordOne("thread_queue_ENGINE-RULE", rulesExecutorService.getQueue().size());
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 构造函数.
     */
    public BcpDecisionContextService() {
        super.initNotify();
        initThreadPool();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        monitorExecutorService.shutdown();
        rulesExecutorService.shutdown();
    }


    private AbstractExecutableBO getInvokeEntryByEvent(ContextWrap contexts) {
        if (StringUtils.isEmpty(FieldSetterReader.Reader.getStringField(contexts, Constant.EVENET_ID))) {
            return null;
        }
        // 提取策略模型
        Long eventid = FieldSetterReader.Reader.getLongField(contexts, Constant.EVENET_ID);
        return getExecutableBO(eventid);
    }

    private AbstractExecutableBO getInvokeEntry(ContextWrap contexts) {
        AbstractExecutableBO executableBO = getInvokeEntryByEvent(contexts);

        if (null == executableBO) {
            logger.info("入口执行查找失败,{}", contexts);
            throw UnknowExecutsetException.newInstance("策略集初始化失败");
        }

        return executableBO;
    }

    @Override
    @SuppressWarnings({"MethodLength"})
    public DecisionExcuteResult invoke(ContextWrap contexts) {
        if (null == contexts || MapUtils.isEmpty(contexts.getContext())) {
            logger.info("参数异常");
            throw UnknowExecutsetException.newInstance("参数异常");
        }
        TimeTrace timeTrace = new TimeTrace();

        AbstractExecutableBO entries = getInvokeEntry(contexts);
        timeTrace.addPoint("getInvokeEntry");

        // 做决策
        DecisionExcuteResult result = entries.invoke(contexts);
        timeTrace.addPoint("invoke");

        postInvokeSavelogAsync(contexts, result);
        timeTrace.addPoint("postInvokeSavelogAsync");

        // 返回决策结果
        logger.info("invoke finished cost={}", timeTrace.getTraceInfo());
        return result;
    }


    private void postInvokeSavelogAsync(final ContextWrap contexts, DecisionExcuteResult decisionExcuteResult) {
        //
    }

}
