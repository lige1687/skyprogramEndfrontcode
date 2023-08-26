package com.sky.mapper;


import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface OrderMapper {


    /*
    注意插入后需要,返回一个 主键值, 用于后续的  事务操作
    后续插入订单的details 的数据 ,需要用到订单的id 这个主键
    返回的 订单的id  , 直接传回我们传入的 orders 对象中的id字段( 通过mapper xml 的编写
    不在mapper中设置是返回不到的
     */
    void insert(Orders orders);


}
