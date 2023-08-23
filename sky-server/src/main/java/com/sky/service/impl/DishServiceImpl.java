package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavourMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import com.sky.mapper.DishFlavourMapper;
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class DishServiceImpl implements DishService {
    @Autowired //别忘了依赖注入
    private DishMapper dishMapper;
    //因为同时需要操作两个表 ,所以 将 两个表都体现在 方法名上


    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishFlavourMapper dishFlavourMapper; // 注入新的 mapper , 同时操作两个表
    // 注意导入  cv代码的时候别多次注入了,  笑死了， 多去查看报错信息!!  获取精确的错误原因

    @Transactional
    public void saveWithFalvour(DishDTO dishDTO) {
        // 事务的实现逻辑, 一个菜品对应 多个口味, flavuor
        //新增菜品 , 这里只是插入菜品 ,  所以传入dish对象即可, 通过new一个dish , Beanutilscopy
        // dishDto 的内容和 dish 不一样, dish多了updatetime和 inserttime  user 等属性, 这需要我们的aop去实现, 也就是通过@autofill注解在mapper
        // dishDto多dish 一个flavour  数组, 能直接beanutils转换 , 只会转换相同属性名字的字段!

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        // 获取insert生成的主键值, 通过 在sql动态中 进行 key的相关 回显得 主键到 dish 对象中
        Long id = dish.getId();


        //新增该菜品的 多个 口味( 使用的是逻辑外键的关联, 需要java开发者去维护
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors.size() > 0 && flavors != null)

        // sql 可以实现 连续插入, 不需要 遍历
        {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            }); // 将insert产生的主键, 插入到每一个dishflavor中
            // Batch 意味批量插入

            dishFlavourMapper.insertBatch(flavors);
        }
    }
//TODO page Query 的编写 , 注意 分页查询ipage 插件所要遵循的 规则, 也可以尝试使用 MP进行编写
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        return null;
    }
// TODO 批量删除 代码的编写
    @Override
    public void deleteBatch(List<Long> ids) {

    }

    //TODO update方法的实现 , 还是withFlavor , 对应的风味也得改变 ! 关联表!
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

    }

    //TODO 起售停售菜品
    @Override
    public void startOrStop(Integer status, Long id) {

    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavourMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList; // 依然是返回vo对象给前端
    }
}