package com.dz.eToSQL.sql.utills;

import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.bean.ColumnDefinition;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.domain.factory.DatabaseStrategyFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

@Component
public class ExcelTableGenerator {

    @Autowired
    private DatabaseStrategyFactory databaseStrategyFactory;

    public String[] generateSQL(File file, UploadRequest uploadRequest) throws Exception {
        String dbType = uploadRequest.getDbType();
        String dbTable = uploadRequest.getDbTable();

        // 获取数据库策略
        DatabaseTypeStrategy strategy = databaseStrategyFactory.getStrategy(dbType);

        // 读取Excel文件
        Workbook workbook = getWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        // 分析列
        List<ColumnDefinition> columns = analyzeColumns(sheet);

        // 生成CREATE TABLE SQL
        String createTableSQL = strategy.createTableSQL(dbTable, columns);

        //生成INSERT语句
        String insertSQL = generateInsertSQL(sheet, dbTable, columns);

        workbook.close();
        return new String[]{createTableSQL, insertSQL};
    }

    private String generateInsertSQL(Sheet sheet, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();
        int rowCount = sheet.getLastRowNum();

        // 跳过标题行（第0行）
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                sql.append("INSERT INTO ").append(tableName).append(" (");

                // Add column names
                for (int i = 0; i < columns.size(); i++) {
                    sql.append(columns.get(i).getName());
                    if (i < columns.size() - 1) {
                        sql.append(", ");
                    }
                }

                sql.append(") VALUES (");

                // Add values
                for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    if (cell != null) {
                        appendCellValue(sql, cell);
                    } else {
                        sql.append("NULL");
                    }

                    if (colIndex < columns.size() - 1) {
                        sql.append(", ");
                    }
                }

                sql.append(");").append("\n");
            }
        }

        return sql.toString();
    }

    private void appendCellValue(StringBuilder sql, Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                sql.append("'").append(escapeSQLString(cell.getStringCellValue())).append("'");
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    sql.append("'").append(new java.sql.Timestamp(cell.getDateCellValue().getTime())).append("'");
                } else {
                    sql.append(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                sql.append(cell.getBooleanCellValue());
                break;
            case BLANK:
                sql.append("NULL");
                break;
            default:
                sql.append("NULL");
        }
    }

    private String escapeSQLString(String value) {
        return value.replace("'", "''");
    }

    private Workbook getWorkbook(File file) throws Exception {
        String fileName = file.getName();
        FileInputStream fis = new FileInputStream(file);

        try {
            if (fileName.endsWith(".xlsx")) {
                return new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                return new HSSFWorkbook(fis);
            }
            throw new MyCustomException(AppHttpCodeEnum.FILE_Valid_FAIL);
        } finally {
            fis.close();
        }
    }


    private void analyzeCell(Cell cell, Set<CellType> types, IntConsumer maxLengthUpdater,
                             Consumer<Boolean> hasDecimalsUpdater) {  // 使用 Consumer<Boolean>
        types.add(cell.getCellType());

        switch (cell.getCellType()) {
            case STRING:
                maxLengthUpdater.accept(cell.getStringCellValue().length());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    types.add(CellType.STRING);
                } else {
                    double value = cell.getNumericCellValue();
                    hasDecimalsUpdater.accept(value != Math.floor(value));
                    maxLengthUpdater.accept(String.valueOf(value).length());
                }
                break;
        }
    }

    private List<ColumnDefinition> analyzeColumns(Sheet sheet) {
        List<ColumnDefinition> columns = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        int rowCount = Math.min(sheet.getLastRowNum(), 100);

        for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
            String columnName = headerRow.getCell(colIndex).getStringCellValue();
            Set<CellType> types = new HashSet<>();
            int maxLength = 0;
            boolean hasDecimals = false;

            // 分析列数据
            for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell cell = row.getCell(colIndex);
                    if (cell != null) {
                        final int[] maxLengthRef = {maxLength};
                        final boolean[] hasDecimalsRef = {hasDecimals};

                        analyzeCell(cell, types,
                                length -> maxLengthRef[0] = Math.max(maxLengthRef[0], length),
                                decimal -> hasDecimalsRef[0] = hasDecimalsRef[0] || decimal);

                        maxLength = maxLengthRef[0];
                        hasDecimals = hasDecimalsRef[0];

                    }
                }
            }

            columns.add(new ColumnDefinition(columnName, types, maxLength, hasDecimals));
        }

        return columns;
    }
}
