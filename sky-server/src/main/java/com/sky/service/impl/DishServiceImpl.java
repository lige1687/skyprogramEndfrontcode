package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavourMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
@Autowired //别忘了依赖注入
    private DishMapper dishMapper;
    //因为同时需要操作两个表 ,所以 将 两个表都体现在 方法名上

    @Autowired
    private DishFlavourMapper dishFlavourMapper; // 注入新的 mapper , 同时操作两个表
    @Transactional
    public void saveWithFalvour(DishDTO dishDTO) {
        // 事务的实现逻辑, 一个菜品对应 多个口味, flavuor
        //新增菜品 , 这里只是插入菜品 ,  所以传入dish对象即可, 通过new一个dish , Beanutilscopy
        // dishDto 的内容和 dish 不一样, dish多了updatetime和 inserttime  user 等属性, 这需要我们的aop去实现, 也就是通过@autofill注解在mapper
        // dishDto多dish 一个flavour  数组, 能直接beanutils转换 , 只会转换相同属性名字的字段!

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        // 获取insert生成的主键值, 通过 在sql动态中 进行 key的相关 回显得 主键到 dish 对象中
        Long id = dish.getId();


        //新增该菜品的 多个 口味( 使用的是逻辑外键的关联, 需要java开发者去维护
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if( flavors.size() > 0 &&  flavors != null)

        // sql 可以实现 连续插入, 不需要 遍历
        { flavors.forEach( dishFlavor -> {
            dishFlavor.setDishId(id);
        }); // 将insert产生的主键, 插入到每一个dishflavor中
            // Batch 意味批量插入

        dishFlavourMapper.insertBatch(flavors) ;
        }
    }
}
