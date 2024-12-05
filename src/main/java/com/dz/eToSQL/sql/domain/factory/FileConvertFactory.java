package com.dz.eToSQL.sql.domain.factory;

import com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract;
import com.dz.eToSQL.sql.service.ConverterService;
import org.springframework.stereotype.Repository;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 16:32
 */
@Repository
public class FileConvertFactory {

    private final ConverterService converterService;

    public FileConvertFactory(ConverterService converterService) {
        this.converterService = converterService;
    }

    public FillConvertAbstract getConverter(String fileType) throws Exception {
       return converterService.createFactory(fileType);
    }
}
