package com.ark.bcp.domain.script;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.datachannel.channel.factory.SqlDataSourceFactory;
import com.ark.bcp.domain.datachannel.channel.factory.SqlDataSourceFactory.SqlConnProperties;
import com.ark.bcp.domain.exception.FailfastException;
import com.google.common.collect.Lists;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

/**
 */
public class SqlExecuteInvoker {
    private final static Logger logger = LoggerFactory.getLogger(SqlExecuteInvoker.class);


    public static Object mockInspectionScript(final String rawScriptSource) {
        try {
            GroovyClassLoader classLoader = new GroovyClassLoader();
            Class<?> groovyScriptClass = classLoader.parseClass(rawScriptSource);
            if (null == groovyScriptClass) {
                throw new FailfastException(null, "加载脚本异常");
            }
            Method loadDataMethod = groovyScriptClass.getMethod("loadData", int.class, String.class, int.class);
            GroovyObject groovyObject = (GroovyObject) groovyScriptClass.newInstance();
            Object result = loadDataMethod.invoke(groovyObject, 0, "0", 1);
            if (null != result && !(result instanceof List)) {
                throw new FailfastException(null, "返回值必须为List类型");
            }
            return result;
        } catch (NoSuchMethodException e) {
            throw new FailfastException(null, "缺少 List loadData(int,String,int){} 函数 ");
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new FailfastException(null, "初始化实例异常");
        } catch (Exception e) {
            throw e;
        }
    }

    public static List<JSONObject> mysqlQuery(
            final String host,
            final Integer port,
            final String database,
            final String usr,
            final String pwd,
            final String sql) {
        SqlConnProperties properties = SqlDataSourceFactory.SqlConnProperties.builder()
                .host(host)
                .port(port)
                .database(database)
                .usr(usr)
                .pwd(pwd).build();

        DataSource dataSource = SqlDataSourceFactory.createSqlDataSource(properties);

        sqlFormatCheck(sql);

        return execute(dataSource, sql);
    }

    public static void sqlFormatCheck(final String sql) {
        // 防呆检测
        String dbType = JdbcConstants.MYSQL;
        try {
            List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
            for (SQLStatement sqlStatement : stmtList) {
                if (!(sqlStatement instanceof SQLSelectStatement)) {
                    throw new FailfastException(null, "只能包含select语句");
                }
                SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
                if (null == sqlSelectStatement.getSelect() || !(sqlSelectStatement.getSelect().getQuery() instanceof MySqlSelectQueryBlock)) {
                    throw new FailfastException(null, "必须包含 limit 关键字，且limit值不大于1000");
                }
                MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) sqlSelectStatement.getSelect().getQuery();
                if (null == queryBlock.getLimit() || ((SQLIntegerExpr) queryBlock.getLimit().getRowCount()).getNumber().intValue() > 1000) {
                    throw new FailfastException(null, "必须包含 limit 关键字，且limit值不大于1000");
                }
            }
        } catch (Exception e) {
            throw new FailfastException(null, "解析sql异常:" + e.getMessage());
        }
    }

    public static List<JSONObject> execute(final DataSource dataSource, final String sql) {

        Connection conn = null;
        ResultSetMetaData rsmd = null;
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement(sql);
            statement.execute();
            resultSet = statement.getResultSet();
            rsmd = resultSet.getMetaData();
            int count = rsmd.getColumnCount();
            List<JSONObject> lines = Lists.newArrayList();
            while (resultSet.next()) {
                JSONObject line = new JSONObject();
                for (int i = 1; i <= count; i++) {
                    line.put(rsmd.getColumnLabel(i), resultSet.getString(i));
                }
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            logger.info("执行sql异常", e);
        } finally {
            try {
                if (null != resultSet) {
                    resultSet.close();
                }
            } catch (Exception e) {
                // donothing
            }
            try {
                if (null != statement) {
                    statement.close();
                }
            } catch (Exception e) {
                // donothing
            }
            try {
                if (null != conn) {
                    conn.close();
                }
            } catch (Exception e) {
                // donothing
            }
        }
        return null;
    }
}
