package com.sky.mapper;


import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;


@Mapper
public interface OrderMapper {


    /*
    注意插入后需要,返回一个 主键值, 用于后续的  事务操作
    后续插入订单的details 的数据 ,需要用到订单的id 这个主键
    返回的 订单的id  , 直接传回我们传入的 orders 对象中的id字段( 通过mapper xml 的编写
    不在mapper中设置是返回不到的
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders 传过来的修改好的订单对象 ( 对数据库中进行覆盖
     */
    void update(Orders orders);

    // 批量查询 订单的状态 , 如查询未支付的  以及超时的所有订单
    // 方法的名字必须明确
// 当然批量查询的话 ,  还可以 通过 list 方法, 传入 orders 的一个对象, 使用动态的sql 去自动的匹配一些传入的参数, 进行对应的查询
    // 这里就 简明扼要的写了, 上边是进阶的!
    @Select("select * from orders where status =#{status} and order_time < #{orderTime} )")
    List<Orders>  getBystatusAndOrderTime(Integer status, LocalDateTime orderTime) ;

    @Select("select * from orders where id= #{id}")
    Orders getById(Long id);

    /**
     * 根据map中的字段 ,查询符合的 字段 的 金融总和 , 这里查询的是营业额
     * @param map 待查询的条件们
     * @return 返回 总营业额,  sum(amount)
     */

    Double sumByMap(HashMap<Object, Object> map);


    Integer countByMap(HashMap<Object, Object> map);

    List<GoodsSalesDTO> getSalesTop(LocalDateTime begin , LocalDateTime end) ;
}
