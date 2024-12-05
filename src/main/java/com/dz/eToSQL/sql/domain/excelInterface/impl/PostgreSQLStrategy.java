package com.dz.eToSQL.sql.domain.excelInterface.impl;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PostgreSQLStrategy implements DatabaseTypeStrategy {
    @Override
    public String createTableSQL(String tableName, List<ColumnDefinition> columns) {
        return "";
    }

    @Override
    public String getColumnDefinition(String columnName, String dataType) {
        return "";
    }

    @Override
    public String mapDataType(Set<CellType> types, int maxLength, boolean hasDecimals) {
        return "";
    }
    // 类似实现...
}
