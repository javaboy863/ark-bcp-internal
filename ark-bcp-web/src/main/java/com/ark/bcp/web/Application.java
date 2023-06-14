
package com.ark.bcp.web;

import com.mryx.monitor.adapter.SpringMonitorInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 启动类.
 *
 * @author wangzheng@missfresh.cn on 2020-09-24.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@ImportResource({"classpath:applicationContext.xml"})
@ComponentScan(basePackages={"com.missfresh.risk.bcp", "com.mryx.grampus.ccs"})
@ServletComponentScan("com.missfresh.risk.bcp.web")
@PropertySource(value = {"classpath:/jdbc.properties"})
public class Application implements WebMvcConfigurer {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        logger.info("risk-bcp SpringBoot Start Success");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SpringMonitorInterceptor()).addPathPatterns("/**");
    }
}
