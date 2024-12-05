package com.dz.eToSQL.generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dz.eToSQL.generator.domain.DO.DataSources;
import com.dz.eToSQL.generator.service.DataSourcesService;
import com.dz.eToSQL.generator.mapper.DataSourcesMapper;
import org.springframework.stereotype.Service;

/**
* @author daizhen
* @description 针对表【data_sources】的数据库操作Service实现
* @createDate 2024-11-22 11:55:57
*/
@Service
public class DataSourcesServiceImpl extends ServiceImpl<DataSourcesMapper, DataSources>
    implements DataSourcesService{

}




