package com.dz.eToSQL.sql.domain.bean.convert;

import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract;
import com.dz.eToSQL.sql.domain.bean.generator.abs.FileGeneratorAbstact;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.dz.eToSQL.sql.domain.bean.generator.ExcelTableGenerator;
import com.dz.eToSQL.sql.utills.FactoryCreater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 16:24
 */
@Component
public class ExcelConvert extends FillConvertAbstract {

    private final FactoryCreater factoryCreater;

    @Autowired
    public ExcelConvert(FactoryCreater factoryCreater) {
        this.factoryCreater = factoryCreater;
    }

    @Override
    public String[] fillToSql(File uploadFile, String type, UploadRequest uploadRequest) {
        super.fillToSql(uploadFile, type, uploadRequest);
        String[] sql = null;
        try {
            sql = ((FileGeneratorAbstact) factoryCreater.createFactory(type)).generateSQL(uploadFile, uploadRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sql;
    }

    @Override
    public Boolean executeSql(String[] sql, UploadRequest uploadRequest) throws ClassNotFoundException, MyCustomException {
        return super.executeSql(sql, uploadRequest);
    }

}
