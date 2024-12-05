package com.dz.eToSQL.sql.utills;

import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.config.ConverterProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @Author daizhen
 * @Description 工厂创建类,根据配置文件创建对应的工厂或策略
 * @Date 2021-1-4 16:28
 */
@Service
public class FactoryCreater {
    private final ConverterProperties converterProperties;
    private final ApplicationContext applicationContext;

    public FactoryCreater(ConverterProperties converterProperties, ApplicationContext applicationContext) {
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
