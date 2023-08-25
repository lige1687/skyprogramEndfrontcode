package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.emitter.ScalarAnalysis;

import java.util.List;

@RestController
@Api(tags = "购物车相关接口")
@Slf4j
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService ;
    //

    @RequestMapping ( "/add")
    @ApiOperation("添加到购物车")
    public Result save (@RequestBody ShoppingCartDTO shoppingCartDTO)

    {
        log.info("添加购物车, 商品信息为{}" , shoppingCartDTO);
        shoppingCartService.save (   shoppingCartDTO);
        return Result.success();
    }
    // 返回购物车里的所有的数据, 所以这里的shoppingCart指的还是 单个购物车物品 的意思
    // 一整个表代表是一个 用户的购物车?  ( 所以不难理解amount 字段是 通过 菜品或者 套餐的 price表示的
    // 返回的泛型 是 一个list, 含有 该用户的所有 购物车里的item ( 这个表述更加合适
    @ApiOperation("查看用户购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>>  list( )
    {
       List<ShoppingCart>  list = shoppingCartService.showShoppingCart( );
        return Result.success(list) ;
    }
    @DeleteMapping
    @ApiOperation("清空购物车")
    public  Result  clean ( )
    {
        shoppingCartService.cleanShoppingCart ( );

    }
}
