

package com.ark.bcp.infr.config;

import com.alibaba.fastjson.JSON;
import com.missfresh.datasource.common.bean.ClusterInfo;
import com.missfresh.datasource.common.bean.ShardingInfo;
import com.missfresh.datasource.common.store.FileStore;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;


/**
 */
@Configuration
@EnableTransactionManagement
public class DbConfig {
    private static Logger logger = LoggerFactory.getLogger(DbConfig.class);
    private static final String PACKAGE = "com.missfresh.risk.bcp.domain.repository.riskbcp";
    private static final String DBCONFIG_MAPPERPATH = "classpath*:/mapper/*.xml";


    @SuppressWarnings("unchecked")
    protected static <T> T createDataSource(DataSourceProperties properties, Class<? extends javax.sql.DataSource> type) {
        return (T) properties.initializeDataSourceBuilder().type(type).build();
    }


    @Bean(name = "riskBcpDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {

        HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
        if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
        }
        String[] info = dataSource.getUsername().split("#");
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setCipher(info[0]);
        clusterInfo.setEnv(info[1]);
        FileStore.fillClusterInfo(clusterInfo);
        List<ShardingInfo> list = clusterInfo.getShardings();
        dataSource.setUsername(list.get(0).getName());
        dataSource.setPassword(list.get(0).getPasswd());
        logger.info("datasource:{}", JSON.toJSONString(properties));
        logger.info("datasource:{}", JSON.toJSONString(list));
        return dataSource;
    }

    /**
     * 创建seqSessionFactory.
     *
     * @param dataSource ""
     * @return ""
     * @throws Exception ""
     */
    @Bean(name = "riskBcpSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(
            @Autowired
            @Qualifier("riskBcpDataSource") javax.sql.DataSource dataSource
    ) throws Exception {
        logger.info("#############  注入 riskBcpSessionFactory  #############");
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //加载Mapper.xml文件
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(DBCONFIG_MAPPERPATH));

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
    @Bean(name = "riskBcpMapperScanner")
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setProcessPropertyPlaceHolders(false);
        configurer.setBasePackage(DbConfig.PACKAGE);
        configurer.setSqlSessionFactoryBeanName("riskBcpSessionFactory");
        return configurer;
    }

    @Bean(name = "riskBcpTxManager")
    public DataSourceTransactionManager riskBcpTxManager(
            @Qualifier(value = "riskBcpDataSource")
                    DataSource dataSource
    ) {
        DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(dataSource);
        return txManager;
    }
}
