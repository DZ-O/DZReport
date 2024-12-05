package com.dz.eToSQL.sql.domain.excelInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SqlServerStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

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
        return String.format("[%s] %s", columnName, dataType); // 使用方括号包裹列名
    }

    @Override
    public String mapDataType(Set<CellType> types, int maxLength, boolean hasDecimals) {
        if (types.contains(CellType.STRING)) {
            return String.format("NVARCHAR(%d)", Math.min(Math.max(maxLength, 1), 4000)); // SQL Server NVARCHAR 最大长度为 4000
        } else if (types.contains(CellType.NUMERIC)) {
            if (hasDecimals) {
                return "DECIMAL(20, 2)"; // 使用 DECIMAL 类型表示小数
            } else {
                return maxLength <= 4 ? "INT" : "BIGINT"; // 使用 INT 或 BIGINT 表示整数
            }
        }
        return "NVARCHAR(255)"; // 默认类型
    }
} 