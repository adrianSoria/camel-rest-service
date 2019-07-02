package org.absit.integrasjon.config;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    @Inject
    public DataSource hikariCP(
        @Value("${spring.datasource.url}") final String dataSourceUrl,
        @Value("${spring.datasource.username}") final String jdbcUser,
        @Value("${spring.datasource.password}") final String jdbcPassword,
        @Value("${spring.datasource.maxConnections}") final Integer maxConnections,
        @Value("${spring.datasource.minConnections}") final Integer minConnections,
        @Value("${spring.datasource.maxInitForsok}") final Integer maxInitForsok,
        @Value("${spring.datasource.driverClassName}") final String driverClassName) {
        final int maxConnectionAgeIMinutter = 60;
        final int queryTimeoutSecs = 30;
        final int idleMaxAge = 20;

        LOGGER.info(String.format("Starter Hikari med "
                + "dataSourceUrl=%s, username=%s, driver=%s, maxConn=%s, minConn=%s",
            dataSourceUrl, jdbcUser, driverClassName, maxConnections, minConnections));
        try {
            final Driver driver = (Driver) Class.forName(driverClassName).newInstance();
            DriverManager.registerDriver(driver);
        } catch (final Exception e) {
            // Dette ignorerer vi - hvis det virkelig er feil her
            // så overlater vi til Hikari å logge dette.
        }

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceUrl);
        config.setUsername(jdbcUser);
        config.setPassword(jdbcPassword);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(maxConnections);
        config.setMinimumIdle(minConnections);
        config.setMaxLifetime(maxConnectionAgeIMinutter * 1000L * 60);

        final String queryTimeoutMs = String.valueOf(1000 * queryTimeoutSecs);
        System.setProperty("oracle.jdbc.ReadTimeout", queryTimeoutMs);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.setAutoCommit(true);
        config.setConnectionTimeout(10000L);
        config.setIdleTimeout(idleMaxAge * 1000L * 60);
        final DataSource ds = new HikariDataSource(config);

        int antallForsoek = 1;
        boolean harConnection = false;
        while (!harConnection) {
            try (Connection ignored = ds.getConnection()) {
                harConnection = true;
                LOGGER.info("Fikk JDBC connection fra poolen ved oppstart etter forsøk nr {}", antallForsoek);
            } catch (final Exception e) {
                LOGGER.info("Fikk ikke JDBC connection fra poolen ved oppstart: {}. Forsøk nr {}", e.getMessage(),
                    antallForsoek++);
                if (antallForsoek > maxInitForsok) {
                    LOGGER.error("Ga opp å få jdbc connection fra poolen etter {} forsøk", maxInitForsok, e);
                }
            }
        }
        return ds;
    }

    @Bean
    @Inject
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
