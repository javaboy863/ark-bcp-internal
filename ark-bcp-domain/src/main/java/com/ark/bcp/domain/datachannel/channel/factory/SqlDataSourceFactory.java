
package com.ark.bcp.domain.datachannel.channel.factory;

import com.google.common.collect.Maps;
import lombok.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.Map;

/**
 */
public class SqlDataSourceFactory {
    private static Map<SqlConnProperties, DataSource> mysqlDataSrouce = Maps.newConcurrentMap();

    public static DataSource createSqlDataSource(final SqlConnProperties properties) {

        if (mysqlDataSrouce.containsKey(properties)) {
            return mysqlDataSrouce.get(properties);
        }
        DataSource dataSource = makeDataSource(properties);
        mysqlDataSrouce.put(properties, dataSource);
        return dataSource;
    }

    private static org.apache.tomcat.jdbc.pool.DataSource makeDataSource(
            final SqlConnProperties properties) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("jdbc:mysql")
                .setHost(properties.getHost())
                .setPort(properties.getPort())
                .setPath("/" + properties.getDatabase())
                .addParameter("useUnicode", "true")
                .addParameter("characterEncoding", "utf-8");

        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setUrl(uriBuilder.toString());
        dataSource.setUsername(properties.getUsr());
        dataSource.setPassword(properties.getPwd());
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(1);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setValidationInterval(30000);
        dataSource.setJmxEnabled(false);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMaxActive(10);
        dataSource.setInitialSize(1);
        dataSource.setMaxWait(10000);
        dataSource.setMinEvictableIdleTimeMillis(30000);
        return dataSource;
    }

    @Data
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SqlConnProperties {
        private String host;
        private Integer port;
        private String database;
        private String usr;
        private String pwd;
    }
}
