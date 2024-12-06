package com.dz.eToSQL.sql.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.emums.FileTypeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract;
import com.dz.eToSQL.sql.domain.factory.FileConvertFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.dz.eToSQL.sql.service.CreatSqlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 14:30
 */
@Service
@Slf4j
public class CreatSqlServiceImpl implements CreatSqlService {

    @Autowired
    private FileConvertFactory fileConvertFactory;

    @Value("${file.sql.temp-path:temp/sql}")
    private String sqlTempPath; // SQL文件临时存储路径

    @Override
    public void fillToSQL(MultipartFile file, UploadRequest uploadRequest, HttpServletResponse response) throws MyCustomException {
        if (!file.isEmpty()) {
            File sqlFile = null;
            try {
                // 创建一个临时文件
                File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
                // 将 MultipartFile 转换为 File
                file.transferTo(tempFile);
                File uploadFile = FileUtil.file(tempFile);
                String type = FileTypeUtil.getType(uploadFile);

                //判断文件类型
                if (!FileTypeEnum.isValidFileType(type)){
                    throw new MyCustomException(AppHttpCodeEnum.FILE_Valid_FAIL);
                }

                FillConvertAbstract converter = fileConvertFactory.getConverter(type);
                if (converter == null){
                    throw new MyCustomException(AppHttpCodeEnum.FILE_CONVERT_FAIL);
                }

                //转换成sql
                String[] sql = converter.fillToSql(uploadFile, type, uploadRequest);
                if (sql == null){
                    throw new MyCustomException(AppHttpCodeEnum.FILE_CONVERT_FAIL);
                }

                //执行sql
                if (uploadRequest.getNeedExecute()){
                    Boolean b = converter.executeSql(sql, uploadRequest);
                    if (!b){
                        throw new MyCustomException(AppHttpCodeEnum.FILE_CONVERT_FAIL);
                    }
                }

                // 生成SQL文件
                String sqlContent = String.join(";\n", sql) + ";"; // 使用分号连接SQL语句
                sqlFile = generateSqlFile(sqlContent, file.getOriginalFilename(), uploadRequest);

                // 设置响应头
                response.setContentType("application/octet-stream");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(sqlFile.getName(), "UTF-8"));

                // 写入响应流
                try (FileInputStream fis = new FileInputStream(sqlFile);
                     BufferedInputStream bis = new BufferedInputStream(fis);
                     OutputStream os = response.getOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int i;
                    while ((i = bis.read(buffer)) != -1) {
                        os.write(buffer, 0, i);
                    }
                    os.flush();
                }

            } catch (Exception e) {
                log.error("生成SQL文件失败", e);
                if (e instanceof MyCustomException) {
                    throw (MyCustomException) e;
                }
                throw new MyCustomException(AppHttpCodeEnum.FILE_CONVERT_FAIL);
            } finally {
                // 清理临时文件
                if (sqlFile != null && sqlFile.exists()) {
                    sqlFile.delete();
                }
            }
        } else {
            throw new MyCustomException(AppHttpCodeEnum.FILE_UPLOAD_FAIL);
        }
    }

    /**
     * 生成SQL文件
     * @param sql SQL内容
     * @param originalFileName 原始文件名
     * @return SQL文件
     */
    private File generateSqlFile(String sql, String originalFileName, UploadRequest uploadRequest) throws IOException {

        // 确保目录存在
        File dir = new File(sqlTempPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String dbName = uploadRequest.getDbName(); // 获取数据库名称
        String dbTable = uploadRequest.getDbTable(); // 获取数据库表名称
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // 使用 dbName、dbTable 和时间戳组合文件名
        String sqlFileName = String.format("%s_%s_%s.sql", dbName, dbTable, timestamp);
        File sqlFile = new File(dir, sqlFileName);

        // 写入SQL内容
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sqlFile))) {
            writer.write("-- Generated from: " + originalFileName + "\n");
            writer.write("-- Generated at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n");
            writer.write(sql);
        }

        return sqlFile;
    }
}
