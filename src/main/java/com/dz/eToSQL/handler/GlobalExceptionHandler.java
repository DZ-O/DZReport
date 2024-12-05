package com.dz.eToSQL.handler;


import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.utills.ResponseResult;
import com.dz.eToSQL.emums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseResult exceptionHandler(Exception e) {

        if (e instanceof MyCustomException){
            e.printStackTrace();
            return ResponseResult.errorResult(((MyCustomException) e).getAppHttpCodeEnum());
        }
        if (e instanceof MethodArgumentNotValidException){
            e.printStackTrace();
            return ResponseResult.errorResult(500,e.getMessage());
        }
        if (e instanceof RuntimeException){
            e.printStackTrace();
            return ResponseResult.errorResult(500,e.getMessage());
        }
        if (e instanceof SQLIntegrityConstraintViolationException){
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.SQL_EXECUTE_FAIL);
        }

        if (e instanceof ClassNotFoundException){
            log.error("ClassNotFoundException:{}",e.getMessage());
            return ResponseResult.errorResult(AppHttpCodeEnum.FILE_UPLOAD_FAIL);
        }
        return ResponseResult.errorResult(500,e.getMessage());
    }
}
