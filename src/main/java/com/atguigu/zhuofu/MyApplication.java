package com.atguigu.zhuofu;

import com.atguigu.mybatis.MapperProxyFactory;

import java.util.List;

public class MyApplication {

    public static void main(String[] args) {
        UserMapper userMapper = MapperProxyFactory.getMapper(UserMapper.class);
//        List<User> result = userMapper.getUser("root");

        User result = userMapper.getUserById(2);
        System.out.println(result);

    }
}
