package com.dz.eToSQL.generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dz.eToSQL.generator.domain.DO.Users;
import com.dz.eToSQL.generator.service.UsersService;
import com.dz.eToSQL.generator.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author daizhen
* @description 针对表【users】的数据库操作Service实现
* @createDate 2024-11-22 11:55:57
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

}




