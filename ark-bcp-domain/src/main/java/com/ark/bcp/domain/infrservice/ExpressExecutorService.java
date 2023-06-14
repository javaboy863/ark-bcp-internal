
package com.ark.bcp.domain.infrservice;

/**
 */
public interface ExpressExecutorService {
    /**
     * 执行表达式
     *
     * @param script
     * @return
     * @throws Exception
     */
    Object execute(String script) throws Exception;

    /**
     * 执行表达式
     *
     * @param script
     * @param conntextParams
     * @return
     * @throws Exception
     */
    Object execute(String script, Object conntextParams) throws Exception;
}
