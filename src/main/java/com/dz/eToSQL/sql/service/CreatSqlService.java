package com.dz.eToSQL.sql.service;

import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 14:29
 */
public interface CreatSqlService {
    void fillToSQL(MultipartFile file, UploadRequest uploadRequest, HttpServletResponse response) throws MyCustomException;
}
