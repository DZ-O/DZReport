package com.dz.eToSQL.sql.domain.excelInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PostgreSQLStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String dbName, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(dbName).append(".").append(tableName).append(" (\n");

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
            return String.format("VARCHAR(%d)", Math.min(Math.max(maxLength, 1), 255));
        } else if (types.contains(CellType.NUMERIC)) {
            if (hasDecimals) {
                return "NUMERIC(20, 2)";
            } else {
                return maxLength <= 4 ? "INTEGER" : "BIGINT";
            }
        }
        return "VARCHAR(255)";
    }
}
