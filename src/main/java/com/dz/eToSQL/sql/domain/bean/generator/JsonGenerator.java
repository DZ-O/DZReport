package com.dz.eToSQL.sql.domain.bean.generator;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact;
import com.dz.eToSQL.sql.domain.dbInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.domain.factory.DatabaseStrategyFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-23 08:46
 */
@Component
public class JsonGenerator extends FileGeneratorAbstact {

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

        // 读取 JSON 文件
        JsonNode rootNode = objectMapper.readTree(file);

        // 分析列
        List<ColumnDefinition> columns = analyzeColumns(rootNode);

        // 生成 CREATE TABLE SQL
        String createTableSQL = strategy.createTableSQL(dbName, dbTable, columns);

        // 生成 INSERT 语句
        String insertSQL = generateInsertSQL(rootNode,dbName, dbTable, columns);

        return new String[]{createTableSQL, insertSQL};
    }

    private List<ColumnDefinition> analyzeColumns(JsonNode rootNode) {
        List<ColumnDefinition> columns = new ArrayList<>();

        // 检查 rootNode 是否是数组
        if (rootNode.isArray() && rootNode.size() > 0) {
            JsonNode firstNode = rootNode.get(0);
            Iterator<String> fieldNames = firstNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = firstNode.get(fieldName);

                boolean isPrimaryKey = fieldName.contains("%pk%");
                fieldName = fieldName.replace("%pk%", "");

                ColumnDefinition column = new ColumnDefinition();
                column.setName(fieldName);
                column.setPrimaryKey(isPrimaryKey);

                if (fieldNode.isTextual()) {
                    column.setTypes(Collections.singleton(CellType.STRING));
                    column.setMaxLength(fieldNode.asText().length());
                } else if (fieldNode.isNumber()) {
                    column.setTypes(Collections.singleton(CellType.NUMERIC));
                    column.setHasDecimals(fieldNode.isFloatingPointNumber());
                    column.setMaxLength(fieldNode.asText().length());
                } else if (fieldNode.isBoolean()) {
                    column.setTypes(Collections.singleton(CellType.BOOLEAN));
                } else if (fieldNode.isArray()) {
                    column.setTypes(Collections.singleton(CellType.STRING));
                    column.setMaxLength(fieldNode.toString().length());
                } else if (fieldNode.isObject()) {
                    column.setTypes(Collections.singleton(CellType.STRING));
                    column.setMaxLength(fieldNode.toString().length());
                } else if (fieldNode.isBinary()) {
                    column.setTypes(Collections.singleton(CellType.STRING));
                    column.setMaxLength(fieldNode.toString().length());
                } else if (fieldNode.isNull()) {
                    column.setTypes(Collections.singleton(CellType.BLANK));
                } else {
                    column.setTypes(Collections.singleton(CellType.STRING));
                    column.setMaxLength(fieldNode.toString().length());
                }

                columns.add(column);
            }
        }

        return columns;
    }

    private String generateInsertSQL(JsonNode rootNode,String dbName, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();

        // 检查 rootNode 是否是数组
        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                sql.append("INSERT IGNORE INTO ").append(dbName+"."+tableName).append(" (");

                for (int i = 0; i < columns.size(); i++) {
                    sql.append(columns.get(i).getName());
                    if (i < columns.size() - 1) {
                        sql.append(", ");
                    }
                }

                sql.append(") VALUES (");

                for (int i = 0; i < columns.size(); i++) {
                    JsonNode fieldNode = node.get(columns.get(i).getName());
                    if (Objects.isNull(fieldNode)) {
                        fieldNode = node.get(columns.get(i).getName() + "%pk%");
                        if (Objects.isNull(fieldNode)) {
                            fieldNode = node.get( "%pk%"+columns.get(i).getName());
                        }
                    }
                    if (fieldNode != null) {
                        if (fieldNode.isTextual()) {
                            sql.append("'").append(fieldNode.asText().replace("'", "''")).append("'");
                        } else if (fieldNode.isNumber()) {
                            sql.append(fieldNode.asText());
                        } else if (fieldNode.isBoolean()) {
                            sql.append(fieldNode.asBoolean());
                        } else {
                            sql.append("'").append(fieldNode.toString().replace("'", "''")).append("'");
                        }
                    } else {
                        sql.append("NULL");
                    }

                    if (i < columns.size() - 1) {
                        sql.append(", ");
                    }
                }

                sql.append("); ");
            }
        }

        return sql.toString();
    }
}
