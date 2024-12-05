package com.dz.eToSQL.sql.domain.factory;

import com.dz.eToSQL.emums.AppHttpCodeEnum;
import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.excelInterface.DatabaseTypeStrategy;
import com.dz.eToSQL.sql.utills.FactoryCreater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseStrategyFactory {

    @Autowired
    private final FactoryCreater factoryCreater;

    public DatabaseStrategyFactory(FactoryCreater factoryCreater) {
        this.factoryCreater = factoryCreater;
    }
    public  DatabaseTypeStrategy getStrategy(String dbType) throws Exception {
        DatabaseTypeStrategy strategy = factoryCreater.createFactory(dbType);
        if (strategy == null) {
            throw new MyCustomException(AppHttpCodeEnum.DB_TYPE_ERROR);
        }
        return strategy;
    }
}
