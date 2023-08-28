package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;

import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
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

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay( // 自己书写的工具类通过 依赖注入的方式 , 来使用
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过webSocket 向客户端推送消息
        // 根据约定的  数据格式进行 数据的推送
        Map map = new HashMap();
        map.put("type" , 1) ;//  约定好1 是 订单提醒, 在支付后完成
        map.put("orderId" , ordersDB.getId()) ; // 利用已经查出来的  订单对象进行 数据的获取
        // 注意订单的主键要想返回 ,需要在mapperxml中加入对应的注解哦
        map.put("content" , "订单号"+ outTradeNo) ;// 拼接一个

        // 将含有三个字符串的 map 转化为json 的数据, 返回给前端
        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
        // 将消息群发给所有的连接  该server 的client


    }


    /**
     * 实现客户催单
     * @param id  要催单的订单的id
     */

    @Override
    public void reminder(Long id) {
        //最好先看看是否 订单存储
        Orders ordersDB = orderMapper.getById(id);
        // 如果 订单 不存在, 或者订单在派送中, 就不能再 进行催单了, 因为这是骑手的事情,不是 商家的
        //   || ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)  , 通过订单内部封装的常量类即可
        if( ordersDB == null  )
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR) ;// 传入订单状态 错误的 异常
            // 统一异常处理即可
        }
        // 推送消息, 给商家端 ,构造一个 map  , 转换为 json 的格式即可
        Map map = new HashMap();
map.put("type" ,2 ) ;
map.put("orderId" , id) ;

map.put("content" , "订单号"+ ordersDB.getNumber()) ;
        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
    }
}
