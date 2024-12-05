package com.dz.eToSQL.web.controller;

import com.dz.eToSQL.exception.MyCustomException;
import com.dz.eToSQL.sql.domain.request.UploadRequest;
import com.dz.eToSQL.sql.service.CreatSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author daizhen
 * @Description
 * @create 2024-12-04 14:29
 */
@RestController
@RequestMapping("/fillToSQL")
public class fillToSQLController {

    @Autowired
    private CreatSqlService creatSqlService;

    @PostMapping("/upload")
    public void upload(
            @ModelAttribute UploadRequest uploadRequest,
            @RequestParam("file") MultipartFile file,
            HttpServletResponse response) throws MyCustomException {
        creatSqlService.fillToSQL(file, uploadRequest, response);
    }
}
