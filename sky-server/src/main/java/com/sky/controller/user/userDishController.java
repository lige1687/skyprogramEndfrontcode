package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class userDishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate ; // 注意,redis 的配置yml, 配置类 创建第三方bean 注入 ( 这里指定的是string 类型的 序列化器
// 所以这里才能直接注入
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        // 查询是否 数据在缓存中 , 根据key进行查询 , 这里的key是 dish_id  , 查询的是分类表,  分类表中锁定  菜品是这样的

        String key = "dish_" +categoryId  ;
        // 注意放入的是什么类型 , 取出就是什么类型
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue()
                .get(key);
        if ( list!= null && list.size()> 0)
        {
            // 如果redis 存在, 就直接返回 无需查询数据库
            return Result.success(list);
        }


        // 如果不在, 就从 数据库中去查 ,并且 存入redis 的缓存中去

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        list = dishService.listWithFlavor(dish); // 查询到 一组vo数据进行返回 , 封装返回 到前端
        // redis 会讲 该范式  的list 序列化为 redis 的string 类型
        redisTemplate.opsForValue().set (key, list) ;
        return Result.success(list);
    }

}
