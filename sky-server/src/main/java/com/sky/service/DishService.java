package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {  /**
 * 条件查询菜品和口味
 * @param dish
 * @return
 */
List<DishVO> listWithFlavor(Dish dish);
    void saveWithFalvour(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void updateWithFlavor(DishDTO dishDTO);

    void startOrStop(Integer status, Long id);
}
