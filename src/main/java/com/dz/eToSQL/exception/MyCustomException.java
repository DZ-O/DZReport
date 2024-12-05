package com.dz.eToSQL.exception;


import com.dz.eToSQL.emums.AppHttpCodeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MyCustomException extends Exception {
   private AppHttpCodeEnum appHttpCodeEnum;


    public MyCustomException(AppHttpCodeEnum appHttpCodeEnum) {
        super(appHttpCodeEnum.getMsg());
        this.appHttpCodeEnum = appHttpCodeEnum;
    }



}

