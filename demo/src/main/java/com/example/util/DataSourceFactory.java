package com.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place to create/reuse a HikariCP DataSource backed by Postgres.
 * Loads config from classpath: config/db.properties.
 */
// 项目与数据库的唯一桥梁 读取数据库配置 创建连接池
public final class DataSourceFactory {

    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);
    private static volatile HikariDataSource dataSource;// 建立连接池对象

    private DataSourceFactory() {}

    public static DataSource getDataSource() {  // 得到唯一数据源实例
        if (dataSource == null) {
            synchronized (DataSourceFactory.class) {
                if (dataSource == null) {
                    dataSource = buildDataSource();
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {  // 获取数据库连接
        return getDataSource().getConnection();
    }

    private static HikariDataSource buildDataSource() {     // 读取数据 创建连接池
        Properties props = loadProps();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        config.setMaximumPoolSize(parseInt(props, "db.pool.maxSize", 10));
        config.setMinimumIdle(parseInt(props, "db.pool.minIdle", 2));
        config.setConnectionTimeout(parseLong(props, "db.pool.connectionTimeoutMs", 30_000L));
        config.setIdleTimeout(parseLong(props, "db.pool.idleTimeoutMs", 600_000L));
        config.setMaxLifetime(parseLong(props, "db.pool.maxLifetimeMs", 1_800_000L));

        // Helpful defaults for Postgres
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        log.info("初始化 HikariCP 数据源：url={}, user={}", config.getJdbcUrl(), config.getUsername());
        return new HikariDataSource(config);
    }

    private static Properties loadProps() {     // 读取配置文件
        Properties props = new Properties();
        try (InputStream in = DataSourceFactory.class.getClassLoader()
                .getResourceAsStream("config/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("找不到配置文件 config/db.properties");
            }
            props.load(in);
        } catch (IOException e) {
            log.error("加载数据库配置失败", e);
            throw new IllegalStateException("加载数据库配置失败", e);
        }
        return props;
    }

    private static int parseInt(Properties p, String key, int defaultVal) {
        try {
            return Integer.parseInt(p.getProperty(key, String.valueOf(defaultVal)));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static long parseLong(Properties p, String key, long defaultVal) {
        try {
            return Long.parseLong(p.getProperty(key, String.valueOf(defaultVal)));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
