package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    // 逻辑主键关联, 如果 菜品的id和 套餐 表的dishid一致, 说明 逻辑外键对应上了
    //TODO 此时是  交集查询, leftjoin 需要进行sql语句的复习


    // 左连接 , left join返回左边表中的数据, 以及右边表中满足 匹配的数据
    // 一般用于 逻辑外键盘 , 或者 关联的两个表的中间表  的匹配
    // 反正都是用于关联的表的
    // 这里就是 逻辑外键, 分类下的菜品id = 菜品的id , 即表示匹配了, 即在该分类下, 也在 菜品表中的 菜品 !
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

}