

package com.ark.bcp.domain.engine.frame.decision.flow;

import com.ark.bcp.domain.entity.EventMessageEntity;
import com.ark.bcp.domain.entity.EventSourceConfigEntity;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class DecisonFlowHandlerExecutionChain {

    private final List<DecisionFlowInterceptorRegistration> registrations = new ArrayList<>();

    public DecisionFlowInterceptorRegistration addInterceptor(@NonNull DecisionFlowInterceptor interceptor) {
        DecisionFlowInterceptorRegistration registration = new DecisionFlowInterceptorRegistration(interceptor);
        Order order = interceptor.getClass().getAnnotation(Order.class);
        if (null != order) {
            registration.setOrder(order.value());
        }
        registrations.add(registration);
        return registration;
    }


    /**
     * Return all registered interceptors.
     */
    private List<DecisionFlowInterceptor> getInterceptors() {
        return this.registrations.stream()
                .sorted(INTERCEPTOR_ORDER_COMPARATOR)
                .map(DecisionFlowInterceptorRegistration::getDecisionFlowInterceptor)
                .collect(Collectors.toList());
    }


    private static final Comparator<DecisionFlowInterceptorRegistration> INTERCEPTOR_ORDER_COMPARATOR = new Comparator<DecisionFlowInterceptorRegistration>() {
        @Override
        public int compare(DecisionFlowInterceptorRegistration o1, DecisionFlowInterceptorRegistration o2) {
            return o1.getOrder() - o2.getOrder();
        }
    };

    public boolean applyPreHandle(final EventSourceConfigEntity eventSourceConfigEntity,
                                  final EventMessageEntity<?> messageEntity) throws Exception {
        List<DecisionFlowInterceptor> interceptors = getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (DecisionFlowInterceptor interceptor : interceptors) {
                if (interceptor.handle(eventSourceConfigEntity, messageEntity)) {
                    return true;
                }
            }
        }
        return false;
    }

}
