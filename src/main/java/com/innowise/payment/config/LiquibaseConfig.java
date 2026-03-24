package com.innowise.payment.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class LiquibaseConfig {

    private static final String EMPTY_CONTEXT = "";
    private static final String MIGRATION_ERROR_MESSAGE = "Failed to apply Liquibase migrations for MongoDB";

    @Value("${spring.mongodb.uri}")
    private String mongoUri;

    @Value("${app.liquibase.change-log}")
    private String changeLogPath;

    @PostConstruct
    public void runLiquibase() {
        try {
            Database database = DatabaseFactory.getInstance()
                    .openDatabase(mongoUri, null, null, null, new ClassLoaderResourceAccessor());

            try (Liquibase liquibase = new Liquibase(changeLogPath, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(EMPTY_CONTEXT);
            }
        } catch (Exception e) {
            throw new BeanInitializationException(MIGRATION_ERROR_MESSAGE, e);
        }
    }
}