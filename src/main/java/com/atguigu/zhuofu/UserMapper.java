package com.atguigu.zhuofu;

import com.atguigu.mybatis.Param;
import com.atguigu.mybatis.Select;

import java.util.List;

public interface UserMapper {
    @Select("select * from user where username = #{username}")
    public List<User> getUser(@Param("username") String username);

    @Select("select * from user where id=#{id}")
    public User getUserById(@Param("id") Integer id);
}
