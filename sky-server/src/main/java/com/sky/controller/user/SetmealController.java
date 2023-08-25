package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @Cacheable (cacheNames = "setmealCache" , key = "#categoryId") // 开启 缓存, 如果在redis中存在
    // 就不会去调用 mapper方法,  如果不存在才 会通过 代理对象的反射调用, 并且 将返回值存储在 redis中
    //这里就是将 result对象存储了
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }

    //  TODO 该方法没有写全,  还有save , delete, update ,  startOrStop 方法
    // 且这些方法 需要加上 cacheEvict注解, 表示清除对应 的缓存, 保持数据的一致性
    // 有些直接全部清除就好, 因为单个清楚还需要去查询, 得不偿失 ( 为了 性能

    @ApiOperation( "新增套餐")
    @PostMapping
@CacheEvict (cacheNames = "setmealCache" , key = "setmealDTO.categoryId") // 清理对应的redis数据
    // 保持数据的一致性, 精确清理
    public  Result save (@RequestBody SetmealDTO setmealDTO)
    {

        setmealService.save(setmealDTO) ;

        return  Result.success() ;

    }

    // 如果是批量删除数据, 直接 全部删除即可

}
