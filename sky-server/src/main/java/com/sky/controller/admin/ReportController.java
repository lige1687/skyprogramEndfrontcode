package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关注解")
@Slf4j

public class ReportController {
    @Autowired
private ReportService reportService;
    private LocalDate begin;
    private LocalDate end;

    // 封装营业额的 vo对象
    // 注意这里是query形式的参数  , 传入的是日期 的 对象, 注意日期是有格式的 ,如果不 标注一下日期的格式是无法正确的封装 到 形式参数的数据中去的
// 表示年月日的格式
    @GetMapping("/turnoverStatistics")
    @ApiOperation( "营业额统计")
public Result<TurnoverReportVO>   turnoverStatistics(    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end )
{
    log.info( "统计营业额日期, {} {} " , begin, end);
     return  Result.success(reportService.getTrunoverStatistics(begin, end)) ;
}

@GetMapping ("/userStatistics")
    @ApiOperation("用户数据统计")
    public  Result<UserReportVO>  userStatistic (     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
{
    log.info("用户数据统计{} , {}" , begin ,end);

    return
             Result.success(reportService.getUserStatistics(begin, end)) ;
}
@GetMapping ("/orderStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> orderStatistics (     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end )
{

    log.info("订单数据统计{} , {}" , begin ,end);

    return
            Result.success(reportService.getOrderStatistics(begin, end)) ;
}

// 纱布了, 方法名一样报错了看了半天没看出来
    @GetMapping ("/top10")
    @ApiOperation("销量排名")
    public Result<SalesTop10ReportVO> top10 (@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin ,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {

        log.info("销量排名统计{} , {}" , begin ,end);

        return
                Result.success(reportService.getSalesTop10(begin, end)) ;
    }
}
