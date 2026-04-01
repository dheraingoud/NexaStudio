package com.nexastudio;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * NexaStudio Backend Application
 * AI-Powered Code Generation Platform
 */
@SpringBootApplication
@EnableAsync
public class NexaStudioApplication {

    private static final Logger log = LoggerFactory.getLogger(NexaStudioApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NexaStudioApplication.class, args);
    }

    /**
     * Fix database schema on startup: make email column nullable.
     */
    @Bean
    CommandLineRunner fixSchema(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ALTER COLUMN email DROP NOT NULL");
                log.info("Schema fix applied: users.email is now nullable");
            } catch (Exception e) {
                log.debug("Schema fix skipped: {}", e.getMessage());
            }
        };
    }
}
