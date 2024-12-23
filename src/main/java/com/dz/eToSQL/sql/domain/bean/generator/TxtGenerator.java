package com.dz.eToSQL.sql.domain.bean.generator;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.domain.factory.DatabaseStrategyFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TxtGenerator extends FileGeneratorAbstact {

    @Autowired
    private DatabaseStrategyFactory databaseStrategyFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String[] generateSQL(File file, UploadRequest uploadRequest) throws Exception {
        String dbType = uploadRequest.getDbType();
        String dbTable = uploadRequest.getDbTable();
        String dbName = uploadRequest.getDbName();

        // 获取数据库策略
        DatabaseTypeStrategy strategy = databaseStrategyFactory.getStrategy(dbType);

        // 读取并分析TXT文件
        List<ColumnDefinition> columns = analyzeColumns(file);

        // 生成CREATE TABLE SQL
        String createTableSQL = strategy.createTableSQL(dbName, dbTable, columns);

        // 生成INSERT语句
        List<String> insertSQLs = generateInsertSQLs(file,dbName, dbTable, columns);
        insertSQLs.add(0, createTableSQL);

        return insertSQLs.toArray(new String[0]);
    }

    private List<ColumnDefinition> analyzeColumns(File file) throws Exception {
        List<ColumnDefinition> columns = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split("\t");
                for (String header : headers) {
                    ColumnNameResult nameResult = translateColumnName(header.trim());
                    columns.add(new ColumnDefinition(nameResult.name, new HashSet<>(), 0, false, nameResult.isPrimaryKey));
                }

                // Read a few rows to determine data types
                int sampleSize = 10;
                String line;
                int rowCount = 0;
                while ((line = br.readLine()) != null && rowCount < sampleSize) {
                    String[] values = line.split("\t", -1);
                    for (int i = 0; i < values.length; i++) {
                        detectDataType(columns.get(i), values[i].trim());
                    }
                    rowCount++;
                }
            }
        }
        return columns;
    }

    private void detectDataType(ColumnDefinition column, String value) {
        Set<CellType> types = column.getTypes();
        if (value.matches("-?\\d+(\\.\\d+)?")) {
            if (value.contains(".")) {
                types.add(CellType.NUMERIC);
                column.setHasDecimals(true);
            } else {
                types.add(CellType.NUMERIC);
            }
        } else {
            types.add(CellType.STRING);
        }
        column.setMaxLength(Math.max(column.getMaxLength(), value.length()));
    }

    private List<String> generateInsertSQLs(File file,String dbName, String tableName, List<ColumnDefinition> columns) throws Exception {
        List<String> insertSQLs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine(); // 跳过标题行
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t", -1);
                if (values.length != columns.size()) {
                    continue;
                }

                StringBuilder sql = new StringBuilder();
                sql.append("INSERT IGNORE INTO ").append(dbName+"."+tableName).append(" (");
                for (int i = 0; i < columns.size(); i++) {
                    sql.append(columns.get(i).getName());
                    if (i < columns.size() - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(") VALUES (");
                for (int i = 0; i < values.length; i++) {
                    String value = values[i].trim();
                    if (value.isEmpty()) {
                        sql.append("NULL");
                    } else {
                        sql.append("'").append(escapeSQLString(value)).append("'");
                    }
                    if (i < values.length - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(")");
                insertSQLs.add(sql.toString());
            }
        }
        return insertSQLs;
    }

    private String escapeSQLString(String value) {
        return value.replace("'", "''");
    }

    private ColumnNameResult translateColumnName(String columnName) {
        boolean isPrimaryKey = columnName.contains("%pk%");
        columnName = columnName.replace("%pk%", "");

        String sanitized = columnName.replaceAll("[\\s\\p{P}]", "");
        String snakeCase = sanitized.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

        if (!snakeCase.matches("^[a-zA-Z].*")) {
            snakeCase = "col_" + snakeCase;
        }

        return new ColumnNameResult(snakeCase, isPrimaryKey);
    }

    private static class ColumnNameResult {
        final String name;
        final boolean isPrimaryKey;

        ColumnNameResult(String name, boolean isPrimaryKey) {
            this.name = name;
            this.isPrimaryKey = isPrimaryKey;
        }
    }
}
