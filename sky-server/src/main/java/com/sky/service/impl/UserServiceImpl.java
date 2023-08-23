package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j

public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
@Autowired
private WeChatProperties wxproperties;

    public static final String WX_LOGIN="GET https://api.weixin.qq.com/sns/jscode2session"; // 请求的微信官方路径
    /**
     * 微信登录, 这里同传统的登录不一样, 无需查询自己的 表格, 因为是存在 微信的官方的, 去请求的是微信官方
     *
     * @param loginDTO
     * 功能主要是获取 openid (通过dto中的 code授权码
     */
    @Override


    public User wxlogin(UserLoginDTO loginDTO) {
        String openid = getOpenid(loginDTO.getCode(), loginDTO);  // 久违的 抽取成 一个私有的方法进行封装( 因为代码较为固定
        // 获取openid 的方法


        if( openid== null)
        {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED) ;// 抛出一个 new异常
        }

        // 3.检测id是否在我们的 数据库中已经存在了? 如果没有就是新用户, 需要进行注册 ( 这里才涉及数据库的操作
        User user = userMapper.getByOpenId(openid);

        // 4. 如果是 新用户, 就需要自动的保存ser对象, 实现自动的注册

        if(user==null)
{
    // 表示新用户, 我们需要注册 ,我们只需要做到初始设置即可, 能获取到的先获取了, 存到表中, 后续 用户会自己完善信息的
     user = User.builder().
             openid(openid).
             createTime(LocalDateTime.now()).
             build();
     userMapper.insert(user);

}
 return user ;

    }
    private String getOpenid(String code, UserLoginDTO loginDTO)
    {
        // 1.调用微信的接口, 获取openid
        // 后端发送请求, 通过 httpClient 框架 , 这里写成了工具类进行操作
        HashMap<String, String> map = new HashMap<>();
        // 第一个参数是 url, 也就是请求的地址, 封装成 常量, 第二个map 即传入的参数们, key自然是 属性名, value自然是值!
        map.put("appid", wxproperties.getAppid()) ;
        map.put("secret", wxproperties.getSecret());
        map.put("js_code", loginDTO.getCode());
        map.put("grant_type","authorization_code");

        // map来传递参数! 合理


        //2.检测openid是否存在, 如果不就代表失败登录
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        // 通过fastjson 进行数据的解析, 解析为一个 java对象!
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }

}
