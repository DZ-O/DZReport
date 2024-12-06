package com.dz.eToSQL.sql.domain.excelInterface;

import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import org.apache.poi.ss.usermodel.CellType;

import java.util.List;
import java.util.Set;

public interface DatabaseTypeStrategy {
    String createTableSQL(String dbName,String tableName, List<ColumnDefinition> columns);
    String getColumnDefinition(String columnName, String dataType);
    String mapDataType(Set<CellType> types, int maxLength, boolean hasDecimals);
}
