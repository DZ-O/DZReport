package com.dz.eToSQL.sql.domain.factory;

import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.service.ConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseStrategyFactory {

    @Autowired
    private final ConverterService converterService;

    public DatabaseStrategyFactory(ConverterService converterService) {
        this.converterService = converterService;
    }
    public  DatabaseTypeStrategy getStrategy(String dbType) throws Exception {
        DatabaseTypeStrategy strategy = converterService.createFactory(dbType);
        if (strategy == null) {
            throw new MyCustomException(AppHttpCodeEnum.DB_TYPE_ERROR);
        }
        return strategy;
    }
}
