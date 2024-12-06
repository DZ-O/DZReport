package com.dz.eToSQL.sql.domain.excelInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OracleStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String dbName, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(dbName).append(".").append(tableName).append(" (\n");

        // 添加列定义
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

        // 添加主键约束
        if (!primaryKeys.isEmpty()) {
            sql.append("    CONSTRAINT pk_").append(tableName).append(" PRIMARY KEY (")
               .append(String.join(", ", primaryKeys))
               .append(")\n");
        }

        sql.append(")");
        return sql.toString();
    }

    @Override
    public String getColumnDefinition(String columnName, String dataType) {
        return String.format("  \"%s\" %s", columnName, dataType);
    }

    @Override
    public String mapDataType(Set<CellType> types, int maxLength, boolean hasDecimals) {
        if (types.contains(CellType.STRING)) {
            return String.format("VARCHAR2(%d)", Math.min(Math.max(maxLength, 1), 4000)); // Oracle VARCHAR2 最大长度为 4000
        } else if (types.contains(CellType.NUMERIC)) {
            if (hasDecimals) {
                return "NUMBER(20, 2)"; // 使用 NUMBER 类型表示小数
            } else {
                return maxLength <= 4 ? "NUMBER" : "NUMBER"; // 使用 NUMBER 类型表示整数
            }
        }
        return "VARCHAR2(4000)"; // 默认类型
    }
}
