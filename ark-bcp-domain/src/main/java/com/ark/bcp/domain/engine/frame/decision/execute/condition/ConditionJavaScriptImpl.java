
package com.ark.bcp.domain.engine.frame.decision.execute.condition;

import com.ark.bcp.domain.engine.frame.decision.execute.ContextWrap;
import com.ark.bcp.domain.entity.ConditionConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ConditionJavaScriptImpl extends AbstractConditionBO {

    private static Logger logger = LoggerFactory.getLogger(ConditionJavaScriptImpl.class);

    public ConditionJavaScriptImpl(ConditionConfigEntity conditionConfigEntity) {
        super(conditionConfigEntity);
    }

    @Override
    protected Object innerConditionInvoke(ContextWrap contextWrap) {
        return null;
    }
}

