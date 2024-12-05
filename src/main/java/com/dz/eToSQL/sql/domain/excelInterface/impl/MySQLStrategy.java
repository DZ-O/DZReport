package com.dz.eToSQL.sql.domain.excelInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

// MySQL策略
@Component
public class MySQLStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");

        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            String dataType = mapDataType(column.getTypes(), column.getMaxLength(), column.isHasDecimals());
            sql.append(getColumnDefinition(column.getName(), dataType));

            if (i < columns.size() - 1) {
                sql.append(",");
            }
            sql.append("\n");
        }

        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");
        return sql.toString();
    }

    @Override
    public String getColumnDefinition(String columnName, String dataType) {
        return String.format("  `%s` %s", columnName, dataType);
    }

    @Override
    public String mapDataType(Set<CellType> types, int maxLength, boolean hasDecimals) {
        if (types.contains(CellType.STRING)) {
            return String.format("VARCHAR(%d) NOT NULL",
                Math.min(Math.max(maxLength * 2, 50), 255));
        } else if (types.contains(CellType.NUMERIC)) {
            if (hasDecimals) {
                return "DECIMAL(20,2) DEFAULT NULL";
            } else {
                return maxLength <= 4 ? "INT NOT NULL" : "BIGINT NOT NULL";
            }
        }
        return "VARCHAR(255) DEFAULT NULL";
    }
}


