

package com.ark.bcp.domain.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 */
public class AsyncUtil {
    private static final Logger logger = LoggerFactory.getLogger(AsyncUtil.class);

    private static final ExecutorService EXECUTOR =
            new ThreadPoolExecutor(
                    10, 40, 60, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<>(1024),
                    new ThreadFactoryBuilder().setNameFormat("asyncUtil-thread-%d").build(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * run.
     *
     * @param task task
     * @return Future
     */
    public static Future<?> run(Runnable task) {
        ThreadPoolExecutor pool = (ThreadPoolExecutor) EXECUTOR;
        logger.info("asyncUtil-getPoolSize:{},getActiveCount:{},getCompletedTaskCount:{},getQueue.size:{}",
                pool.getPoolSize(), pool.getActiveCount(),
                pool.getCompletedTaskCount(),
                pool.getQueue().size());
        return EXECUTOR.submit(task);
    }
}
