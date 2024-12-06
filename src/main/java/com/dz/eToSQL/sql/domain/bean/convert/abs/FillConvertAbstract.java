package com.dz.eToSQL.sql.domain.bean.convert.abs;

import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.config.DatabaseDriverProperties;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author daizhen
 * @Description 将文件转化为sql并执行
 * @create 2024-12-04 16:20
 */
@Component
public abstract class FillConvertAbstract {

    @Autowired
    private DatabaseDriverProperties databaseDriverProperties;


    public String[] fillToSql(File uploadFile, String type, UploadRequest uploadRequest) {
        return null;
    }

    public Boolean executeSql(String[] sqlStatements, UploadRequest uploadRequest) throws ClassNotFoundException, MyCustomException {
        String dbType = uploadRequest.getDbType();
        String dbIp = uploadRequest.getDbIp();
        Integer dbPort = uploadRequest.getDbPort();
        String dbName = uploadRequest.getDbName();
        String dbUser = uploadRequest.getDbUser();
        String dbPassword = uploadRequest.getDbPassword();

        // 构建数据库连接 URL
        String dbUrl = loadDriverClass(dbType, dbIp, dbPort, dbName);

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // 关闭自动提交
            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {


                // 执行CREATE TABLE语句
                statement.execute(sqlStatements[0] + ";");

                // 使用PreparedStatement批量执行INSERT语句
                if (sqlStatements.length > 1) {
                    int length = sqlStatements.length;
                    // 分割SQL语句
                    for (int n = 1; n < length; n++) {
                        String[] sqlStr = sqlStatements[n].split(";");

                        // 找到第一条INSERT语句的模式
                        String insertPattern = findInsertPattern(sqlStr[0]);
                        if (insertPattern != null) {
                            try (PreparedStatement pstmt = connection.prepareStatement(insertPattern)) {
                                // 从第二条SQL语句开始，都是INSERT语句
                                for (int i = 0; i < sqlStr.length; i++) {
                                    String insertSql = sqlStr[i].trim();
                                    System.out.println(insertSql);
                                    if (!insertSql.isEmpty()) {
                                        // 解析VALUES部分
                                        List<String> values = parseValues(insertSql);
                                        // 设置参数
                                        setParameters(pstmt, values);
                                        // 添加到批处理
                                        pstmt.addBatch();

                                        // 每1000条提交一次
                                        if ((i + 1) % 1000 == 0) {
                                            pstmt.executeBatch();
                                            connection.commit();
                                            pstmt.clearBatch();
                                        }
                                    }
                                }
                                // 执行剩余的批处理
                                pstmt.executeBatch();
                            }
                        }
                    }
                }

                // 提交事务
                connection.commit();
                return true;

            } catch (Exception e) {
                // 发生异常时回滚
                connection.rollback();
                throw new MyCustomException(AppHttpCodeEnum.SQL_EXECUTE_FAIL);
            }
        } catch (SQLException e) {
            throw new MyCustomException(AppHttpCodeEnum.DB_CONNECT_FAIL);
        }
    }

    private String findInsertPattern(String firstInsert) {
        Pattern pattern = Pattern.compile("INSERT IGNORE INTO ([^\\(]+)\\(([^\\)]+)\\) VALUES\\s*\\([^\\)]+\\)");
        Matcher matcher = pattern.matcher(firstInsert);
        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String columns = matcher.group(2);
            return String.format("INSERT IGNORE INTO %s (%s) VALUES (%s)",
                tableName,
                columns,
                Arrays.stream(columns.split(","))
                      .map(c -> "?")
                      .collect(Collectors.joining(", "))
            );
        }
        return null;
    }

    private List<String> parseValues(String insertSql) {
        // 提取VALUES括号中的值
        Pattern pattern = Pattern.compile("VALUES\\s*\\((.+)\\)");
        Matcher matcher = pattern.matcher(insertSql);
        if (matcher.find()) {
            String valuesStr = matcher.group(1);
            return parseValuesList(valuesStr);
        }
        return new ArrayList<>();
    }

    private List<String> parseValuesList(String valuesStr) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : valuesStr.toCharArray()) {
            if (c == '\'' && (current.length() == 0 || current.charAt(current.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            values.add(current.toString().trim());
        }

        return values;
    }

    private void setParameters(PreparedStatement pstmt, List<String> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i).trim();

            if ("NULL".equals(value)) {
                pstmt.setNull(i + 1, Types.NULL);
            } else if (value.startsWith("'") && value.endsWith("'")) {
                // 字符串值
                pstmt.setString(i + 1, value.substring(1, value.length() - 1));
            } else {
                try {
                    // 尝试作为数字处理
                    if (value.contains(".")) {
                        pstmt.setDouble(i + 1, Double.parseDouble(value));
                    } else {
                        pstmt.setLong(i + 1, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    // 如果无法解析为数字，则作为字符串处理
                    pstmt.setString(i + 1, value);
                }
            }
        }
    }

    @NotNull
    private String loadDriverClass(String dbType, String dbIp, Integer dbPort, String dbName) throws ClassNotFoundException, MyCustomException {
        // 转换为小写以匹配配置
        String dbTypeLower = dbType.toLowerCase();

        // 获取数据库配置
        DatabaseDriverProperties.DatabaseConfig dbConfig = databaseDriverProperties.getConfig().get(dbTypeLower);
        if (dbConfig == null) {
            throw new MyCustomException(AppHttpCodeEnum.DB_TYPE_ERROR);
        }

        // 加载驱动
        Class.forName(dbConfig.getDriverClassName());

        // 构建URL
        if ("sqlite".equals(dbTypeLower) || "h2".equals(dbTypeLower)) {
            // H2特殊处理，因为只需要数据库名
            return String.format(dbConfig.getUrlTemplate(), dbName);
        } else {
            // 其他数据库使用完整连接信息
            return String.format(dbConfig.getUrlTemplate(), dbIp, dbPort, dbName);
        }
    }
}
