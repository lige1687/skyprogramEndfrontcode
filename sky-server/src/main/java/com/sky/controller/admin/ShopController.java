package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api( tags = "店铺相关接口")
@RequestMapping("/admin/shop")
public class ShopController {
    public static final String key ="SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    @ApiOperation( "店铺状态设置")
    @PutMapping("/{status}")
    public  Result  setStatus(@PathVariable  Integer status)
    {
        log.info("店铺状态 {}", status== 1? " 营业中": "打烊了");
        redisTemplate.opsForValue().set(key, status);
        return Result.success();
    }
    @ApiOperation("店铺状态查询")
    @GetMapping("/status") // 一个是路径参数, 一个是路径, 用这种形式区分 get和put请求,  这就是restful 风格
    public  Result getStatus   ()
    {
     Integer shopStatus = (Integer)redisTemplate.opsForValue().get(key);
        log.info("店铺状态 {}" , shopStatus == 1? "营业中" : "打烊了");

        if ( shopStatus ==null )
        {
            return Result.error("无法查询到对应 信息");
        }

        return  Result.success(shopStatus); // 成功就传回 状态给前端 !!! 毕竟是一个 get请求, 必须传回封装的数据
    }


}
