package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {
    /**
     * 统计指定时间内  营业额相关的数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTrunoverStatistics
            (LocalDate begin , LocalDate end);

    /**
     * 统计指定 时间区间内的用户 信息相关的数据
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics
            (LocalDate begin , LocalDate end);

    /**
     * 统计指定时间区间内的 订单的数据
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);
}
