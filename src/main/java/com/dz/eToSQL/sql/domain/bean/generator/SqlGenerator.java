package com.dz.eToSQL.sql.domain.bean.generator;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.domain.factory.DatabaseStrategyFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-23 17:06
 */
@Component
public class SqlGenerator extends FileGeneratorAbstact {

    @Autowired
    private DatabaseStrategyFactory databaseStrategyFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String[] generateSQL(File file, UploadRequest uploadRequest) throws Exception {
        List<String> sqlStatements = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    sql.append(line);
                    if (line.endsWith(";")) {
                        sqlStatements.add(sql.toString());
                        sql.setLength(0);
                    }
                }
            }

            if (sql.length() > 0) {
                sqlStatements.add(sql.toString());
            }
        }
        return sqlStatements.toArray(new String[0]);
    }
}
