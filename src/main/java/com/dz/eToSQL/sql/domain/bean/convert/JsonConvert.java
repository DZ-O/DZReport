package com.dz.eToSQL.sql.domain.bean.convert;

import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.bean.convert.abs.FillConvertAbstract;
import com.dz.eToSQL.sql.domain.factory.FileGeneratorFactory;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 16:29
 */
@Component
public class JsonConvert extends FillConvertAbstract {

    private final FileGeneratorFactory fileGeneratorFactory;

    @Autowired
    public JsonConvert(FileGeneratorFactory fileGeneratorFactory) {
        this.fileGeneratorFactory = fileGeneratorFactory;
    }

    @Override
    public String[] fillToSql(File uploadFile, String type, UploadRequest uploadRequest) {
        super.fillToSql(uploadFile, type, uploadRequest);
        String[] sql = null;
        try {
            sql = fileGeneratorFactory.getGenerator(type).generateSQL(uploadFile, uploadRequest);
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
