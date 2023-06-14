

package com.ark.bcp.infr.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Properties;


/**
 */
@Configuration
public class ManageDbConfig implements EnvironmentAware {
    private static Logger logger = LoggerFactory.getLogger(ManageDbConfig.class);
    private static final String PACKAGE = "com.missfresh.risk.bcp.domain.repository.manage";
    private static final String MANAGEDBCONFIG_MAPPERPATH = "classpath*:/manage_mapper/*.xml";
    private static final String MANAGEDBCONFIG_PREFIX = "antispam.manage.datasource";

    private Properties properties;

    @Bean(name = "manageDbDataSource")
    public DataSource dataSource() {
        logger.info("#############  注入 manageDbDataSource  #############");
        DataSource dataSource = new DataSource();
        dataSource.setUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setMaxIdle(200);
        dataSource.setMinIdle(5);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setValidationInterval(30000);
        dataSource.setJmxEnabled(false);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMaxActive(200);
        dataSource.setInitialSize(5);
        dataSource.setMaxWait(10000);
        dataSource.setMinEvictableIdleTimeMillis(30000);

        return dataSource;
    }

    @Bean(name = "manageDbSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(
            @Autowired
            @Qualifier("manageDbDataSource") DataSource dataSource
    ) throws Exception {
        logger.info("#############  注入 riskBcpSessionFactory  #############");
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //加载Mapper.xml文件
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(MANAGEDBCONFIG_MAPPERPATH));

        return sqlSessionFactoryBean.getObject();
    }

    /**
     * 禁止使用 @MapperScan.
     * mybatis-spring 2.0.2
     *
     * @return MapperScannerConfigurer
     * @see MapperScannerConfigurer  processPropertyPlaceHolders 默认为ture,
     * 导致在DefaultListableBeanFactory#doGetBeanNamesForType 遍历了所有的BeanDefinition 导致提前初始,加载不上proerties
     */
    @Bean(name = "manageDbMapperScanner")
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setProcessPropertyPlaceHolders(false);
        configurer.setBasePackage(ManageDbConfig.PACKAGE);
        configurer.setSqlSessionFactoryBeanName("manageDbSessionFactory");
        return configurer;
    }

    /**
     * Set the {@code Environment} that this component runs in.
     *
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        Iterable<ConfigurationPropertySource> sources = ConfigurationPropertySources.get(environment);
        Binder binder = new Binder(sources);
        BindResult<Properties> bindResult = binder.bind(MANAGEDBCONFIG_PREFIX, Properties.class);
        properties = bindResult.get();
    }
}
