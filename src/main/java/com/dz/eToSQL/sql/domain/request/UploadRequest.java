package com.dz.eToSQL.sql.domain.request;

import lombok.Data;

/**
 * @author daizhen
 * @Description 上传文件后执行sql的数据库信息
 * @create 2024-12-04 14:44
 */
@Data
public class UploadRequest {

    // 数据库ip
    private String dbIp;

    // 数据库端口
    private Integer dbPort;

    // 数据库名称
    private String dbName;

    // 数据库用户名
    private String dbUser;

    // 数据库密码
    private String dbPassword;

    // 数据库类型
    private String dbType;

    // 数据库表
    private String dbTable;

    // 是否需要执行
    private Boolean needExecute;


}
