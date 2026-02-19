package com.penny.expense.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Prod-profile DataSource configuration.
 *
 * Render.com provides the database URL in standard URI format:
 *   postgresql://user:password@host:port/dbname
 *
 * The PostgreSQL JDBC driver requires credentials to be passed separately
 * from the URL — it cannot parse the userinfo (user:password@) component
 * out of a jdbc:postgresql:// URL when HikariCP also sets username/password.
 *
 * This config parses the URI manually and builds a clean HikariDataSource
 * with credentials separated, so neither Spring nor HikariCP ever sees a
 * URL with embedded credentials.
 */
@Configuration
@Profile("prod")
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String rawDatabaseUrl;

    @Bean
    public DataSource dataSource() {
        URI uri = parseUri(rawDatabaseUrl);

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s?sslmode=require",
                uri.getHost(),
                uri.getPort() == -1 ? 5432 : uri.getPort(),
                uri.getPath());

        String[] userInfo = uri.getUserInfo() != null
                ? uri.getUserInfo().split(":", 2)
                : new String[]{"", ""};

        String username = userInfo[0];
        String password = userInfo.length > 1 ? userInfo[1] : "";

        log.info("Connecting to PostgreSQL at {}:{}{} as user '{}'",
                uri.getHost(),
                uri.getPort() == -1 ? 5432 : uri.getPort(),
                uri.getPath(),
                username);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Pool settings for Render free tier
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setMaximumPoolSize(5);
        config.setKeepaliveTime(300_000);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    private URI parseUri(String url) {
        // Strip jdbc: prefix if someone accidentally added it
        String clean = url.startsWith("jdbc:") ? url.substring(5) : url;
        try {
            return new URI(clean);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Invalid DATABASE_URL: '" + url + "'. " +
                    "Expected format: postgresql://user:password@host:port/dbname", e);
        }
    }
}
