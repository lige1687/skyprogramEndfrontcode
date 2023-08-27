package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单的定时任务类
 */
@Component
@Slf4j
public class OrderTask {

@Autowired
    private OrderMapper orderMapper;
    @Scheduled( cron = "0 * * * * ?") // 每分钟触发一次
    public void  processTimeOutOrder( )
    {

    log.info("定时处理超时订单{}" , LocalDateTime.now());

    //查询是否有未付款的 订单,  且下单时间超过15min  , 通过下单时间 进行计算, 下单时间 是否< 当前时间 -15min
    LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);

    // 获取到 减去 15分后的时间, 看是否 超时 , orders 类下定义了 一些常量, 这里表示未支付的
    List<Orders> ordersList = orderMapper.getBystatusAndOrderTime(Orders.PENDING_PAYMENT, localDateTime);

    // 查到  所有的未支付且超时的订单
    if( ordersList != null && ordersList.size()>0)
    {
        for (Orders orders : ordersList) {
            orders.setStatus( Orders.CANCELLED ); // 遍历设置为已经取消
            orders.setCancelReason("订单超时");
            orders.setCancelTime(LocalDateTime.now()); // 同时设置取消原因和时间! 业务逻辑的闭环
            orderMapper.update(orders); // 最后调用 持久层进行 数据的保存
        }
    }

}

    /**
     * 处理一直处于派送中的订单的状态
     */
    @Scheduled (cron = "0 0 1 * * ?  ")  // 每天凌晨 一点发送 一次 ( 所以减去 一个小时就是昨天未完成的订单
    public  void   processDeliveryOrder ( )
{
    log.info("定时处理一直在 派送中的订单{}", LocalDateTime.now());
    // 查询什么状态的什么时间的 mapper函数 , 更加通用!
    LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);
    List<Orders> ordersList = orderMapper.getBystatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, localDateTime);

    if( ordersList != null && ordersList.size()>0)
    {
        for (Orders orders : ordersList) {
            orders.setStatus( Orders.COMPLETED ); // 遍历设置为已经 完成
            // 只要你写的足够通用, 复用性够好, 那么 就可以大幅减少工作量, 只是修修改改的事情
             orderMapper.update(orders); // 最后调用 持久层进行 数据的保存
        }
    }

}


}
