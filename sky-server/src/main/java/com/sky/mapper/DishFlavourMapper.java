package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavourMapper {
    /**
     * 批量插入 口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);
    // TODO 查询菜品关联的所有的 风味 , 该 方法的 xml sql还未书写
      @Select("")
    List <DishFlavor >getByDishId ( Long id);

}
