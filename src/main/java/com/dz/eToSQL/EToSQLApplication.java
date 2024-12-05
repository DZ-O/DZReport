package com.dz.eToSQL;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.dz.eToSQL.generator.mapper")
@EnableConfigurationProperties
public class EToSQLApplication {

    public static void main(String[] args) {
        SpringApplication.run(EToSQLApplication.class, args);
    }

}
