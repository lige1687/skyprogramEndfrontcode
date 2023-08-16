package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavourMapper {
    /**
     * 批量插入 口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);
}
