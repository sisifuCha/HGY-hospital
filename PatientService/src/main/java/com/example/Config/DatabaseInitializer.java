package com.example.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
@ConditionalOnProperty(name = "app.db.init", havingValue = "true", matchIfMissing = false)
public class DatabaseInitializer {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema-dev.sql"));
            populator.addScript(new ClassPathResource("data-dev.sql"));
            populator.execute(dataSource);
        } catch (Exception e) {
            // Log but do not prevent application from starting
            System.err.println("DatabaseInitializer failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
