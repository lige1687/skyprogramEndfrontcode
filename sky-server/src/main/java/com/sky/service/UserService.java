package com.sky.service;


import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    // 想想需要实现, 封装什么接口
    /**
     * 微信登录的接口方法 , 思考返回的数据, 以及 传递的参数
     */
    User wxlogin(UserLoginDTO loginDTO) ;
}
