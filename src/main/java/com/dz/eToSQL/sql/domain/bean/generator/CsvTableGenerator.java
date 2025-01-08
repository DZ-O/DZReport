package com.dz.eToSQL.sql.domain.bean.generator;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact;
import com.dz.eToSQL.sql.domain.dbInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.domain.factory.DatabaseStrategyFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.dz.eToSQL.sql.utills.PinyinUtil;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CsvTableGenerator extends FileGeneratorAbstact {

    @Autowired
    private DatabaseStrategyFactory databaseStrategyFactory;

    @Override
    public String[] generateSQL(File file, UploadRequest uploadRequest) throws Exception {
        Charset charset = detectCharset(file);
        List<ColumnDefinition> columns = analyzeColumns(file, charset);
        DatabaseTypeStrategy strategy = databaseStrategyFactory.getStrategy(uploadRequest.getDbType());
        String createTableSQL = strategy.createTableSQL(uploadRequest.getDbName(), uploadRequest.getDbTable(), columns);
        List<String> insertSQLs = generateInsertSQLs(file, charset,uploadRequest.getDbName(), uploadRequest.getDbTable(), columns);
        insertSQLs.add(0, createTableSQL);
        return insertSQLs.toArray(new String[0]);
    }

    private Charset detectCharset(File file) throws IOException {
        Metadata metadata = new Metadata();
        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            DefaultDetector detector = new DefaultDetector();
            MediaType mediaType = detector.detect(inputStream, metadata);
            String charsetName = metadata.get("Content-Encoding");
            return charsetName != null ? Charset.forName(charsetName) : Charset.forName("UTF-8");
        }
    }

    private List<ColumnDefinition> analyzeColumns(File file, Charset charset) throws IOException {
        List<ColumnDefinition> columns = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(",");
                for (String header : headers) {
                    ColumnNameResult nameResult = translateColumnName(header.trim());
                    columns.add(new ColumnDefinition(nameResult.name, new HashSet<>(), 0, false, nameResult.isPrimaryKey));
                }

                // Read a few rows to determine data types
                int sampleSize = 10;
                String line;
                int rowCount = 0;
                while ((line = br.readLine()) != null && rowCount < sampleSize) {
                    String[] values = line.split(",");
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

    private List<String> generateInsertSQLs(File file, Charset charset, String dbName,String tableName, List<ColumnDefinition> columns) throws IOException {
        List<String> insertSQLs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            String headerLine = br.readLine(); // Skip header line
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",",-1);
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
                        Set<CellType> types = columns.get(i).getTypes();
                        if (types.contains(CellType.NUMERIC)) {
                            if (value.matches("-?\\d+(\\.\\d+)?")) {
                                sql.append(value);
                            } else {
                                sql.append("NULL");
                            }
                        } else if (types.contains(CellType.BOOLEAN)) {
                            sql.append(Boolean.parseBoolean(value));
                        } else if (types.contains(CellType.STRING)) {
                            sql.append("'").append(escapeSQLString(value)).append("'");
                        } else if (types.contains(CellType.BLANK)) {
                            sql.append("NULL");
                        } else {
                            sql.append("'").append(escapeSQLString(value)).append("'");
                        }
                    }
                    if (i < values.length - 1) {
                        sql.append(", ");
                    }
                }
                sql.append(");");
                insertSQLs.add(sql.toString());
            }
        }
        return insertSQLs;
    }

    private String escapeSQLString(String value) {
        return value.replace("'", "''");
    }

    private ColumnNameResult translateColumnName(String columnName) {
        String finalName;
        boolean isPrimaryKey = columnName.contains("%pk%");
        columnName = columnName.replace("%pk%", "");

        if (columnName.contains("{{") && columnName.contains("}}")) {
            int start = columnName.indexOf("{{") + 2;
            int end = columnName.indexOf("}}");
            if (start < end) {
                String customName = columnName.substring(start, end).trim();
                if (isValidColumnName(customName)) {
                    return new ColumnNameResult(customName, isPrimaryKey);
                }
            }
        }

        String sanitized = columnName.replaceAll("[\\s\\p{P}]", "");
        String pinyin = PinyinUtil.getPinyin(sanitized, "");
        String snakeCase = pinyin.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

        if (!snakeCase.matches("^[a-zA-Z].*")) {
            snakeCase = "col_" + snakeCase;
        }

        return new ColumnNameResult(snakeCase, isPrimaryKey);
    }

    private boolean isValidColumnName(String columnName) {
        return columnName.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
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
