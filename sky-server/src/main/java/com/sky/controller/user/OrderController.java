package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userOrderController")
@Slf4j
@Api( tags = "订单相关操作")
@RequestMapping("/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService ;

    /**
     * 处理用户下单, 返回 微信支付页面
     */
    @ApiOperation("用户下单接口")
    @PostMapping("/submit")
    // 注意result 的包是否导入正确了, apach 也有这样的类名
    // 返回的数据封装 vo !
    public Result<OrderSubmitVO> submit (@RequestBody OrdersSubmitDTO ordersSubmitDTO)
    {
        log.info("用户下单{}" ,ordersSubmitDTO);
OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return  Result.success(orderSubmitVO);
    }
}
