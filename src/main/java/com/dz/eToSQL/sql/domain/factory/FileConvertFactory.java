package com.dz.eToSQL.sql.domain.factory;

import com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract;
import com.dz.eToSQL.sql.utills.FactoryCreater;
import org.springframework.stereotype.Repository;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 16:32
 */
@Repository
public class FileConvertFactory {

    private final FactoryCreater factoryCreater;

    public FileConvertFactory(FactoryCreater factoryCreater) {
        this.factoryCreater = factoryCreater;
    }

    public FillConvertAbstract getConverter(String fileType) throws Exception {
       return factoryCreater.createFactory(fileType);
    }
}
