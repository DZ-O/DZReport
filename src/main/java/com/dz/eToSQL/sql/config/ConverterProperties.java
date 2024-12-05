package com.dz.eToSQL.sql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author daizhen
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.converter")
public class ConverterProperties {
    private Map<String, String>  factory;
}
