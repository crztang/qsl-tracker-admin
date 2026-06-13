package com.qsl.tracker.config;

import com.alibaba.druid.pool.DruidDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConnectionLogger implements ApplicationRunner {

    private final DataSource dataSource;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        String activeProfiles = String.join(",", environment.getActiveProfiles());
        if (activeProfiles.isBlank()) {
            activeProfiles = "default";
        }

        String dataSourceClass = dataSource.getClass().getName();
        String jdbcUrl = null;
        String username = null;
        String driverClassName = null;

        if (dataSource instanceof DruidDataSource druidDataSource) {
            jdbcUrl = druidDataSource.getUrl();
            username = druidDataSource.getUsername();
            driverClassName = druidDataSource.getDriverClassName();
        }

        log.info("Database connection summary: profiles={}, datasourceClass={}, jdbcUrl={}, username={}, driverClassName={}",
                activeProfiles, dataSourceClass, jdbcUrl, username, driverClassName);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Database actual connection: product={} {}, catalog={}, schema={}, url={}",
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    safe(connection.getCatalog()),
                    safe(connection.getSchema()),
                    metaData.getURL());
        } catch (Exception ex) {
            log.warn("Failed to read database metadata on startup", ex);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
