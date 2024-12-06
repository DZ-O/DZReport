package com.dz.eToSQL.sql.domain.bean.generator.abs;

import com.dz.eToSQL.sql.domain.request.UploadRequest;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-06 08:55
 */
@Component
public abstract class FileGeneratorAbstact {
    public String[] generateSQL(File file, UploadRequest uploadRequest) throws Exception {
        return null;
    }
}
