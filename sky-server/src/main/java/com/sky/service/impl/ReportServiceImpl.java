package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl  implements ReportService {
    /**
     * 查询范围内的 营业额数据
     * @param begin 开始
     * @param end 结束
     * @return 返回 封装vo对象
     */

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper; // 在明细表里记录的是 不同的菜品和套餐的 个数( 由于 不同风味的在这里不算同一条数据
    // 但是我们统计菜品和商品的总份数 , 这里又 看做不同口味的是一种 菜品了
    // 以及检测 对应的 orders表中 中订单 的状态是否是已经完成的 ? 如果不是还不算 销量, 因为根本没卖出去( details只是每一个订单 的所有内含有的菜品 ,看不出 是否该订单 已经完成了
    // 所以需要引入 ordersMapper 来检测对应 的 订单状态!

    // 纱布了, 语句写完再去看报错!!! , 你这里还没写完呢, 看个几把报错

    @Override
    public TurnoverReportVO getTrunoverStatistics(LocalDate begin,
                                                  LocalDate end)
    {
        // 计算出 日期list和营业额list

        //1. 计算 日期list
        ArrayList<LocalDate> dateArrayList = new ArrayList<>();
        dateArrayList.add(begin);
        while(! begin.equals(end))

        {
            begin = begin.plusDays(1) ; //  加到 list集合中
            dateArrayList.add(begin);
        }
        // 最后就 将list中的所有日期 遍历出来, 通过stringUtils


        List<Double> turnoverList  = new ArrayList<Double>() ; // 存放每天的营业额


        // 2 . 查询营业额 , 一个道理 , 遍历并且查询 ,到一个list中, 最后通过stringUtil join 来转换为一个 string ( 因为前端要求是一个string!!
        for (LocalDate date : dateArrayList) {
            // 查询当天 已完成的 订单的 金额总和

            // select sum(amount) from orders where order_time >  begintime and order_time  <  endtime and status = 5
            //  5 表示已经完成的状态 , 通过 ordertime去锁定当天, 通过聚合函数去查询总和
            LocalDateTime begintime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endtime = LocalDateTime.of(date, LocalTime.MAX);//  注意datetime和 localdate 的区别, 一个有 时分秒, 一个没有
            // 所以这里通过 of 进行 时分秒的 添加,  date本来只有日期, 通过of +  常量 创建 一个一天的开始的 datetime 对象
            HashMap<Object, Object> map = new HashMap<>();
            map.put("begin" , begintime) ;
            map.put("end" , endtime);
            map.put("status", Orders.COMPLETED) ; //  因为查询的是 已经完成的 订单的金额总和
            Double turnover = orderMapper.sumByMap (map ) ;       // 直接根据map进行查询即可! 传入一个map  , 通用性更加高
                  if (turnover == null) {
                      turnover = 0.0 ;
                  }   // 如果是空, 设置为 0.0 即可

                    turnoverList.add(turnover) ;

        }

        return TurnoverReportVO.builder().dateList(StringUtil.join(",", dateArrayList)).  // 将list转换为 string 进行传入
                turnoverList(StringUtil.join("," , turnoverList)).
                build() ;
    }

    /**
     * 统计用户相关信息
     * @param begin 开始时间
     * @param end 结束时间
     * @return 返回 封装好的vo对象, 将查出来的数据全部转换为  string  (这是前端要求的
     * // 这里返回的是 每日新增用户总量,  截止目前总用户量,   以及日期 区间
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //1. 计算 日期list
        ArrayList<LocalDate> dateArrayList = new ArrayList<>();
        dateArrayList.add(begin);
        while(! begin.equals(end))

        {
            begin = begin.plusDays(1) ; //  加到 list集合中
            dateArrayList.add(begin);
        }
        // 最后就 将list中的所有日期 遍历出来, 通过stringUtils

        List<Integer> newUserlist = new ArrayList<>() ;
        List<Integer> totalUserlist = new ArrayList<>() ;


        // 根据 createtime 统计用户的总量( 锁定到 这一天的所有的 新增创建用户!  , count(id) 表示查询id字段的 数量, 因为是主键, 也就代表了所有的 用户个数
        //select count(id) from user where create_time < ?  and create_time > ?

        // select count(id) from user where create_time <  ? 查询截止到每一天的 所有的用户个数

        for (LocalDate date : dateArrayList) {
            // 基于每一天 , 去查询 新增用户和总的 用户
            // 最终封装到两个list中去
            // 注意我们数据库的时间, 是localdateTime格式的, 精确到时分秒, 这里需要进行转换!
            LocalDateTime begintime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endtime = LocalDateTime.of(date, LocalTime.MAX);//  注意datetime和 localdate 的区别, 一个有 时分秒, 一个没有
            HashMap<Object, Object> map = new HashMap<>();

            map.put( "end"  , endtime) ; // 只有end查询的是 截止到目前为止所有的用户
            Integer totalCount = userMapper.countByMap(map);
            map.put("begin" , begintime) ; // 两个都传入的时候 锁定的是某一天的总用户
            Integer newCount = userMapper.countByMap(map);

            totalUserlist.add(totalCount);
            newUserlist.add(newCount) ;

        }
        // 写一个动态的sql 来拼接  , 即可只写一个mapper方法

        return UserReportVO.builder().dateList(StringUtil.join( ",",dateArrayList )).
        totalUserList(StringUtil.join("," , totalUserlist))   // 表示每一天的 用户总和的list
                .newUserList(StringUtil.join(",", newUserlist)) // newuserlist 表示 每一天的 新增用户的 数据构成的list( 对应x轴的每一天
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateArrayList = new ArrayList<>();
        dateArrayList.add(begin);
        while(! begin.equals(end))

        {
            begin = begin.plusDays(1) ; //  加到 list集合中
            dateArrayList.add(begin);
        }
        List<Integer> effectiveOrderslist = new ArrayList<>() ;  // 存储每一天的有效 订单
        List<Integer> totalOrderslist = new ArrayList<>() ; // 存储每一天的总订单

        for (LocalDate date : dateArrayList) {

            // 查询订单总数 ( select count (id) from  orders where order_time < begin ands order_time > end
            // 同样要进行 date类型的转换

            LocalDateTime begintime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endtime = LocalDateTime.of(date, LocalTime.MAX);//  注意datetime和 localdate 的区别, 一个有 时分秒, 一个没有
            HashMap<Object, Object> map = new HashMap<>();
map.put("begin" , begintime) ;
map.put("end", endtime) ;
Integer totalOrders= orderMapper.countByMap( map); // 已有的方法不能用,我们就去手写一个!  countByMap , 根据map传入的集合 进行查询
            // 查询每一天的有效订单个数 , 有效订单数, 多一个 status 条件查询, 表明是有效的

       map.put("status", Orders.COMPLETED) ; // 5 表示有效, 在我们 的orders 中写好 的常量


            // 这里mapper 的方法实际可以封装成一个 内置的方法进行复用的
            Integer effectiveOrders = orderMapper.countByMap(map) ;

            effectiveOrderslist .add(effectiveOrders) ;
            totalOrderslist.add(totalOrders) ;

        }
// 前三个list 整理好了, 加下来是三个 vo需要封装的其他量
        // 1. 区间内的订单总数, 这里直接 遍历 我们存好的集合即可 , 不过使用 list 的stream 进行 计算更快

        //  reduce 表示合并,  合并 所有元素的总和 (用 Integer下的 sum 字段进行表示 ,最后通过 get获取这个总和
        Integer totalOrderCount = totalOrderslist.stream().reduce(Integer::sum).get();
        Integer ValidOrderCount = effectiveOrderslist.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;  // 这里必须这么写, 不然 无法识别 完成率对象, 因为if不一定成立!

        // 2. 最后计算订单完成率, 就是有效的除 总的订单
        if ( totalOrderCount !=0) {  //细节, 只有 除数不为0 的时候才能  进行除法!  且整型必须有一个转换为  double 的, 因为结果是double的
            orderCompletionRate = ValidOrderCount.doubleValue() / totalOrderCount ;
        }

        return OrderReportVO.builder().dateList(StringUtil.join("," , dateArrayList)).
                validOrderCountList(StringUtil.join("," ,  effectiveOrderslist)).
                orderCountList(StringUtil.join("," ,  totalOrderslist)).totalOrderCount(totalOrderCount)
                .validOrderCount(ValidOrderCount).
                orderCompletionRate(orderCompletionRate).
                build();
    }

    /**
     * 获取销量前十的 菜品或者套餐的 排行榜( 意味着 排序
     * @param begin 开始日期
     * @param end 结束日期
     * @return 返回的是封装好的vo对象
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // 注意 , 表示的是多个字段之间的间隔, 没有 , 表示起别名!!  给前边的整体起别名
        //  查询所用的 sql 语句 select  od.name , sum(od.number) number from order_details od , orders o where od.order_id = o.id and o.status = 5 and 时间区间在一 一段时间内!
        // group by  od.name  order by  number desc limit 0 , 10 根据菜品或者套餐的名称进行 分组展示!( 如果select 了多个 字段,  必须指定一个字段来分组
        // limit 起始数据 是从哪个开始, 查询几个数据, 这里就是从第0 个开始看 10个即可
        // 设计 逻辑主键的 匹配, od的 订单id和 真正的订单id 匹配才是 一个合法的数据, 并且判断状态是否是已经完成的, 5, 不然不能算到销量里去


        //这里调用的 top10 所需要传入的是LocalDateTime类型的begin和end , 需要手动封装一下
        LocalDateTime begintime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endtime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop(begintime, endtime);

        // 得到一个list集合, 但是返回的应该是一个vo , 此时需要手动的进行vo 的封装
        // 我们需要的是将name 拿出来, 拼接成一个字符串, 以  , 进行分割( 使用stringUtil, 并且准备一个name 的list
        // for循环可以, 但是繁琐, 此时可以通过stream流进行处理, 更加间接 effictive
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        // map表示执行一个函数 , 该函数是对 list中每一个元素这里也就是dto都要执行的方法  ,  此时 整个主题的结果就是 每一个dto所get到的name
        // collect 表示将list 每一个执行getname获取到的name 收集为一个什么 集合? 这里是一个新的list
        String namelist = StringUtil.join(",", names);


        // 来, 在尝试理解一下! stream针对list 的每一个元素进行的操作, 是一种函数式的编程!
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberlist = StringUtil.join(",", numbers);


        return SalesTop10ReportVO.builder().nameList(namelist).numberList(numberlist).build();
    }

}
