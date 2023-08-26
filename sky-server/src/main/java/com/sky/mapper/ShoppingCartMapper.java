package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    // 根据动态的条件查询数据 , 可能是 一个或者多个 , 所以用list
    // 选择实体类去接受返回的数据
    // 因为我们要的查询条件, 这个类中都有

    // 为什么传递参数的时候不用 dto 了这里? 因为dto中的属性没有 userID
    // 我们需要 根据userid进行查询 , 所以我们只能 dto转成 entity 并且进行userid 的手动赋值
    List<ShoppingCart>  list ( ShoppingCart shoppingCart)
     ;
    @Update("update shopping_cart set number =#{number}  where id = #{id}  ")
    void updateNumberById ( ShoppingCart shoppingCart) ;

    // 因为id是 插入后自动生成的主键, 所以无需 插入指定

    /**
     * 插入一条购物车数据
     *
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) values " +
            "(#{name} ,#{image} , #{userId} .#{dishId} ,  #{setmealId} , #{dishId} ,#{dishFlavor} ,#{amount} , #{createTime} )")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除 购物车数据
     * @param currentId 当前的用户id
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}  ")
    void deleteByUserId(Long currentId);


}
