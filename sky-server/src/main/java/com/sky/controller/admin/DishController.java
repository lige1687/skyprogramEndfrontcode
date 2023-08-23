package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Api (tags = "菜品相关接口")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController

{
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private  DishService dishService;
    @ApiOperation("新增菜品")
    @PostMapping()
    // 通过请求体接受数据
    public Result save ( @RequestBody  DishDTO dishDTO)
    {
        log.info("菜品保存,{}" , dishDTO );
        dishService.saveWithFalvour(dishDTO);
        // 清理缓存数据
        // 构造key , 也就是看我们这个save影响到了哪个key , 对应的就是当前新增的菜品所属的分类 ,需要清楚
        // 一整个清除, 因为 一个分类id对应一个value , 里边是一个list存储菜品们
        String key = "dish_"+ dishDTO.getCategoryId();
        cleanCache(key); // 删除该菜品所影响的 整个分类
        return Result.success() ;
    }
    /**
     * 菜品分类查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分类查询")
    // 注意 ,使用 ipage插件, 就得遵循一定的规则
    // page result 封装的是 总条数, 以及记录每一条数据的list
    public  Result<PageResult> page (DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("菜品分类查询{}" , dishPageQueryDTO)
                ;

        //TODO 实现page Query方法 ,复习 ipage 插件的使用
        PageResult pageResult =dishService.pageQuery(dishPageQueryDTO) ;
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public  Result delete (@RequestParam List <Long> ids)
    {
        log.info("批量删除 菜品{}" , ids);
        // 如果定位去删除对应的redis, 还是需要查询, 烦 , 而且ids可能删除多个,  得不偿失
        // 不如直接将dish_开头的 key 全部删除

        cleanCache("dish_*");

        dishService.deleteBatch(ids) ;
        return
                Result.success();
    }

    // getbyid 方法实现
    // TODO 可以使用 mybatis plus 进行常规代码的生成哦 ! ( 不过复杂的逻辑还得自己书写

    //update 修改操作
    @ApiOperation("修改菜品")
    @PutMapping ()
    public Result update( @RequestBody DishDTO dishDTO)
    {
        // 此时 是清理所有缓存? 还是只清理一个? 具体看影响
        // 如修改分类, 影响了两个 缓存数据!
        // 如果 查询影响的分类, 再去清理, 有点繁琐 , 这里依然采取 所有的dish_ 都清理掉
        cleanCache("dish_*");
        log.info("修改菜品 {}" , dishDTO);
        dishService.updateWithFlavor(dishDTO) ;
        return  Result.success();
    }
    @ApiOperation("起售和停售")
    @PostMapping("/status/{status}")
    public  Result<String> startOrstop ( @PathVariable Integer status , Long id)
    {
        // 通过 状态和id 锁定一个 菜品 !! 进行起售和停售
        dishService.startOrStop ( status , id) ;


        cleanCache("dish_*");
        // 可以 通过菜品的id 查询到对应的 分类的id 再去删除对应的dish分类的key , 但是查询的开销太大, 得不偿失
        return
                Result.success() ;
    }
    // 比较通用, 抽取一个方法出来
    // 传入的就是要删除的 pattern 模式了, 对应的是keys 的 命名规律
    private  void cleanCache(String pattern )
    {
        Set keys = redisTemplate.keys(pattern); // 先获取所有的keys , 再去删除 , dish_* 表示 dish开头的 所有字段
        redisTemplate.delete(keys) ; // 支持删除集合
    }

}
