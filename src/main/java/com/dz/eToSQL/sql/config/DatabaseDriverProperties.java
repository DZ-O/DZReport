package com.dz.eToSQL.sql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "database.driver")
public class DatabaseDriverProperties {
    private Map<String, DatabaseConfig> config;

    @Data
    public static class DatabaseConfig {
        private String driverClassName;
        private String urlTemplate;
    }
}
