package com.dz.eToSQL.sql.domain.dbInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.dbInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SQLiteStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String dbName, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(dbName).append(".").append(tableName).append(" (\n");

        List<String> primaryKeys = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            sql.append("    ").append(getColumnDefinition(column.getName(), mapDataType(column.getTypes(), column.getMaxLength(), column.isHasDecimals())));

            if (column.isPrimaryKey()) {
                primaryKeys.add(column.getName());
            }

            if (i < columns.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append("\n");
            }
        }

        if (!primaryKeys.isEmpty()) {
            sql.append("    PRIMARY KEY (")
               .append(String.join(", ", primaryKeys))
               .append(")\n");
        }

        sql.append(")");
        return sql.toString();
    }

    @Override
    public String getColumnDefinition(String columnName, String dataType) {
        return String.format("\"%s\" %s", columnName, dataType);
    }

    @Override
    public String mapDataType(Set<CellType> types, int maxLength, boolean hasDecimals) {
        if (types.contains(CellType.STRING)) {
            return "TEXT"; // SQLite doesn't have a specific VARCHAR type
        } else if (types.contains(CellType.NUMERIC)) {
            if (hasDecimals) {
                return "REAL"; // Use REAL for floating point numbers
            } else {
                return "INTEGER"; // Use INTEGER for whole numbers
            }
        }
        return "TEXT"; // Default type
    }
}
