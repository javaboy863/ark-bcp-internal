

package com.ark.bcp.domain.datachannel.channel.mq.base;

import com.mryx.sentinel.Entry;
import com.mryx.sentinel.SphU;
import com.mryx.sentinel.Tracer;
import com.mryx.sentinel.context.ContextUtil;
import com.mryx.sentinel.slots.block.BlockException;

/**
 */
public abstract class AbstractSentinelMdcConsumer extends AbstractMdcConsumer {
    @Override
    final public boolean processMdc(Object object) {
        Entry entry = null;
        try {
            ContextUtil.enter(getSentinelResourceName());
            entry = SphU.entry(getSentinelResourceName());
            processInSentinel(object);
            return true;
        } catch (BlockException e) {
            return handleBlock(object, e);
        } catch (Exception e) {
            Tracer.trace(e);
            return handleException(object, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public String getUrl() {
        return getSentinelResourceName();
    }

    public boolean handleBlock(Object object, BlockException blockException) {
        return false;
    }

    public boolean handleException(Object object, Exception exception) {
        return false;
    }

    /**
     * 获取sentinel名称.
     *
     * @return ""
     */
    public abstract String getSentinelResourceName();

    /**
     * 处理json消息.
     *
     * @param object ""
     * @return ""
     */
    public abstract boolean processInSentinel(Object object);

}
