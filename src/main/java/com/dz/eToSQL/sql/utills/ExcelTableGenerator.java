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

        // 生成INSERT语句
        String insertSQL = generateInsertSQL(sheet, dbTable, columns);

        workbook.close();
        return new String[]{createTableSQL, insertSQL};
    }

    private String generateInsertSQL(Sheet sheet, String tableName, List<ColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder();

        // 找到表头行
        int headerRowIndex = 0;
        while (headerRowIndex <= sheet.getLastRowNum()) {
            Row currentRow = sheet.getRow(headerRowIndex);
            if (isValidHeaderRow(currentRow)) {
                break;
            }
            headerRowIndex++;
        }

        // 从表头的下一行开始生成INSERT语句
        for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && isValidDataRow(row)) {
                sql.append("INSERT INTO ").append(tableName).append(" (");

                // 添加列名
                for (int i = 0; i < columns.size(); i++) {
                    sql.append(columns.get(i).getName());
                    if (i < columns.size() - 1) {
                        sql.append(", ");
                    }
                }

                sql.append(") VALUES (");

                // 添加值
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

    private List<ColumnDefinition> analyzeColumns(Sheet sheet) throws MyCustomException {
        List<ColumnDefinition> columns = new ArrayList<>();

        // 查找第一个非空行作为表头
        Row headerRow = null;
        int headerRowIndex = 0;
        int lastRowNum = sheet.getLastRowNum();

        while (headerRowIndex <= lastRowNum) {
            Row currentRow = sheet.getRow(headerRowIndex);
            if (isValidHeaderRow(currentRow)) {
                headerRow = currentRow;
                break;
            }
            headerRowIndex++;
        }

        // 如果没有找到有效的表头行，抛出异常
        if (headerRow == null) {
            throw new MyCustomException(AppHttpCodeEnum.FILE_CONVERT_FAIL);
        }

        // 分析数据行（从表头的下一行开始）
        int rowCount = Math.min(lastRowNum - headerRowIndex, 100);

        for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
            // 获取中文列名并转换
            String chineseColumnName = headerRow.getCell(colIndex).getStringCellValue();
            String englishColumnName = translateColumnName(chineseColumnName);

            Set<CellType> types = new HashSet<>();
            int maxLength = 0;
            boolean hasDecimals = false;

            // 分析列数据
            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(headerRowIndex + i);
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

            columns.add(new ColumnDefinition(englishColumnName, types, maxLength, hasDecimals));
        }

        return columns;
    }

    /**
     * 判断是否为有效的表头行
     * @param row Excel行
     * @return 是否有效
     */
    private boolean isValidHeaderRow(Row row) {
        if (row == null) {
            return false;
        }

        // 检查行中是否至少有一个非空单元格
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 转换列名
     * @param columnName 原始列名
     * @return 转换后的列名
     */
    private String translateColumnName(String columnName) {
        // 检查是否包含{{}}格式的内容
        if (columnName.contains("{{") && columnName.contains("}}")) {
            // 提取{{}}中的内容
            int start = columnName.indexOf("{{") + 2;
            int end = columnName.indexOf("}}");
            if (start < end) {
                String customName = columnName.substring(start, end).trim();
                // 验证提取的名称是否符合数据库命名规范
                if (isValidColumnName(customName)) {
                    return customName;
                }
            }
        }

        // 如果没有{{}}或提取的名称无效，则进行中文转换
        String sanitized = columnName.replaceAll("[\\s\\p{P}]", "");
        String pinyin = PinyinUtil.getPinyin(sanitized, "");
        String snakeCase = pinyin.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

        // 确保以字母开头
        if (!snakeCase.matches("^[a-zA-Z].*")) {
            snakeCase = "col_" + snakeCase;
        }

        return snakeCase;
    }

    /**
     * 验证列名是否符合数据库命名规范
     * @param columnName 列名
     * @return 是否有效
     */
    private boolean isValidColumnName(String columnName) {
        // 列名必须字母开头，只能包含字母、数字和下划线
        return columnName.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    /**
     * 判断是否为有效的数据行（至少有一个单元格不为空）
     * @param row Excel行
     * @return 是否有效
     */
    private boolean isValidDataRow(Row row) {
        if (row == null) {
            return false;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return true;
            }
        }
        
        return false;
    }
}
