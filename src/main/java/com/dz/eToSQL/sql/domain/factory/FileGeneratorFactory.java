package com.dz.eToSQL.sql.domain.factory;

import com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract;
import com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact;
import com.dz.eToSQL.sql.utills.FactoryCreater;
import org.springframework.stereotype.Component;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-06 08:47
 */
@Component
public class FileGeneratorFactory {

    private final String key = "Generator";
    private final FactoryCreater factoryCreater;

    public FileGeneratorFactory(FactoryCreater factoryCreater) {
        this.factoryCreater = factoryCreater;
    }

    public FileGeneratorAbstact getGenerator(String fileType) throws Exception {
        return factoryCreater.createFactory(fileType+ key);
    }
}
