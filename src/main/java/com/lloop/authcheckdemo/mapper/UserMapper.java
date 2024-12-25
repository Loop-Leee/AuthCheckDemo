package com.lloop.authcheckdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lloop.authcheckdemo.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
* @author lloop
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-12-24 21:53:21
* @Entity com.lloop.authcheckdemo.model.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where account = #{Account}")
    User selectByAccount(String Account);
}




