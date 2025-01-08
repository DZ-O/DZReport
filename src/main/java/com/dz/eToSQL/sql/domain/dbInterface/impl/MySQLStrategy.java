package com.dz.eToSQL.sql.domain.dbInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.dbInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// MySQL策略
@Component
public class MySQLStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String dbName, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(dbName).append(".").append(tableName).append(" (\n");

        // 添加列定义
        List<String> primaryKeys = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            sql.append("    ").append(column.getName()).append(" ")
               .append(mapDataType(column.getTypes(), column.getMaxLength(), column.isHasDecimals()));

            if (column.isPrimaryKey()) {
                primaryKeys.add(column.getName());
            }

            if (i < columns.size() - 1 || !primaryKeys.isEmpty()) {
                sql.append(",");
            }
            sql.append("\n");
        }

        // 添加主键约束
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


