

package com.ark.bcp.infr.configrations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 */
@Configuration
public class ThreadPoolConfigration {

    @Bean("alertThreadPool")
    public ThreadPoolTaskExecutor alertThreadPool() {
        ThreadPoolTaskExecutor executor =  new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors()*2);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors()*4);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(1024);
        executor.setThreadNamePrefix("BCP_ALERT-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
