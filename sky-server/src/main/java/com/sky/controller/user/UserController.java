package com.sky.controller.user;


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@Api(tags = "c端用户相关接口")
@RequestMapping("/user/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired // 注入 propertites类! 进行配置属性 的获取
    private JwtProperties jwtProperties ;
    @ApiOperation("微信登录 ")
    // 接受json 的DTO数据, 所以必须 加responseBody , 请求体的数据获取
    @PostMapping("/login")
   public Result<UserLoginVO>  login (@RequestBody UserLoginDTO userLoginDTO)
    {
        log.info("微信登录功能执行中 {}" , userLoginDTO);
        // 有异常直接到了全局的异常处理器 去解决了, 所以这里正常写代码就可以了
        User user = userService.wxlogin(userLoginDTO);
        // 返回的是 openid和 id和 jwt令牌(自然是需要你手动生成的) 进行数据的 赋值即可
// 注意该方法的第三个参数是 一个map ,所以需要手动创建 , 传入的是 用户id 以及它的描述(表明是用户id 的常量字段
        Map<String, Object> Claims = new HashMap<>();
        // 通过jwt 常量类 标注, 更为规范
        Claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), Claims);
        // token令牌创建好了, 还有 vo对象剩下的 id和openid没获取
        // 使用 VO 的 builder 方法进行链式编程
        UserLoginVO userLoginVO = UserLoginVO.builder().
                token(token).
                id(user.getId()).
                openid(user.getOpenid()).
                build();


        return Result.success(userLoginVO);
    }

}
