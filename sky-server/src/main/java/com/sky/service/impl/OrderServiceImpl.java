package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;

import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    /**
     * 用户下单  ,主要涉及两个表的 操作 , 数据库的表设计如此, 所以要加事务?

     */
    @Override

    @Transactional // 涉及多个 mapper操作, 必须保持一致性

    // 这里也可以交给前端去做, 如 微信官方就可以检查是否为空,  但是为了健壮性, 还是写了吧
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //  先校验 用户提交数据的合法性,  业务的异常清空, 地址簿为空, 没有地址, 以及购物车是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
if ( addressBook ==null )
{
    throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL) ;
    // 封装异常类进行返回
}
        Long currentId = BaseContext.getCurrentId();
// 我们这里 通过list实现 多种条件查询的满足 , 查询返回多个数据, 如果你传入的信息只能查询一个数据
        // 直接访问返回结果的list.get(0) 即可
        // list 的mapper和动态sql 的思想可以学习一下
        ShoppingCart cart = new ShoppingCart();
        cart.setId(currentId);


        List<ShoppingCart> list = shoppingCartMapper.list(cart);
        //封装的是 购物车里的 所有的item
        if ( list == null || list.size() ==0)
        {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        }

        // 1. 订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO , orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID); // 设置支付状态
        orders.setStatus(Orders.PENDING_PAYMENT); // 设置订单状态 为等待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis())); // 使用时间戳(这里转换为 string类型的,通过 javaapi) 作为订单号
        // 通过地址簿, 查询 手机号( 存在于 地址id对应的数据里了, 直接查就可以, 也可以使用冗余字段进行记录
        // 这里是前者
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee()); // 获取收货人
        orders.setUserId(currentId); // 通过threadLocal

        // 拷贝完, 还需要挨个去对应,  哪个属性没有设置? 漏掉了?
orderMapper.insert(orders);

List<OrderDetail> orderDetaillist = new ArrayList <>() ;
        //2. 订单detail 中插入 若干条数据 , 即查看购物车的数据
        for (ShoppingCart shoppingCart : list) {
            // 将每一个 购物车的数据,封装为 details  的一个item , 插入
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            // 注意orderid即所属哪个id是没有的, 需要你通过上边的插入进行主键的返回
            // 并且在这里set orderid
            orderDetail.setOrderId(orders.getId()); //他返回直接返回到 orders这个订单 对象中 的id字段了
            orderDetaillist.add( orderDetail) ;
        }

        // 可以单个插入, 可以批量插入 , 这里选择 批量插入, 效率高
         orderDetailMapper.insertBatch(orderDetaillist) ;
        // 3.清空购物车 ( 清空redis和mysql 的数据( 保持数据一致性
shoppingCartMapper.deleteByUserId(currentId);


//4. 封装VO

        return OrderSubmitVO.builder().id(orders.getId()) // 这个order订单的主键 ,通过 mapperxml 的插入获取 到了orders对象
                // 也就是mapper层方法的 形式参数中 去了, 直接获得
                // 我们对 orders 的赋值上边都已经做好了!
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

    }
}
