package com.dz.eToSQL.emums;

public enum AppHttpCodeEnum {
    // 成功
    SUCCESS(200,"操作成功"),
    FILE_UPLOAD_FAIL(5000,"文件上传失败"),
    FILE_Valid_FAIL(5001,"文件上传失败！不支持该类型文件转换"),
    SYSTEM_ERROR(50002,"请求太频繁，请稍后再试"),
    FILE_CONVERT_FAIL(5003,"文件转换失败"),
    SQL_EXECUTE_FAIL(5004,"sql执行失败"),
    DB_TYPE_ERROR(5005,"暂不支持该数据库类型转换"),
    DB_CONNECT_FAIL(5006,"数据库连接失败"),
    FACTORY_REGISTER_FAIL(5007,"执行工厂注册失败"),
    ;

    int code;
    String msg;

    AppHttpCodeEnum(int code, String errorMessage){
        this.code = code;
        this.msg = errorMessage;
    }



    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
