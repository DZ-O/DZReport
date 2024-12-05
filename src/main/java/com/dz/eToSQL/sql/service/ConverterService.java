package com.dz.eToSQL.sql.service;

import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.config.ConverterProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ConverterService {
    private final ConverterProperties converterProperties;
    private final ApplicationContext applicationContext;

    public ConverterService(ConverterProperties converterProperties, ApplicationContext applicationContext) {
        this.converterProperties = converterProperties;
        this.applicationContext = applicationContext;
    }

    public String getClassName(String fileType) {
        return converterProperties.getFactory().get(fileType.toLowerCase());
    }

    public <T> T createFactory(String fileType) throws MyCustomException {
        String className = getClassName(fileType);
        if (className == null) {
            throw new MyCustomException(AppHttpCodeEnum.FACTORY_REGISTER_FAIL);
        }
        try {
            return (T) applicationContext.getBean(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new MyCustomException(AppHttpCodeEnum.FACTORY_REGISTER_FAIL);
        }
    }

}
