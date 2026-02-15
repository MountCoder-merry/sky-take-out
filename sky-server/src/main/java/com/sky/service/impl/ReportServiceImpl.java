package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
 
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
 
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //1.准备日期列表数据 dateList ---> 近7日：[5-14，5-15，...5-20]
        List<LocalDate> dateList = new ArrayList<LocalDate>();
        //循环插入日期数据
        while (!begin.isAfter(end)){
            //注意：小心死循环
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        log.info("dateList = {}" , dateList);
 
 
        //2.准备营业额列表数据 turnoverList
        List<Double> turnoverList = new ArrayList<>();
 
        //营业额=订单状态为已完成的订单金额
        //查询order表，条件: 状态-已完成， 下单时间
        //要统计每一天的营业额，我们可以循环遍历dateList列表，得到每一天的日期，再进行条件查询
        dateList.forEach(date -> {
            //注意一定要用sum，不能用count，count统计出来的是一列有几条数据，sum统计出来的是所有订单金额之和
            //select sum(amount) from orders where status = 5 and order_time between '2025-10-09 00:00:00' and '2025-10-09 23:59:59.999999';
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN)); //2024-05-14 00:00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));   //2024-05-14 23:59:59.999999
            Double turnover = orderMapper.sumByMap(map);
            //用来处理没有营业额的情况
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        });
 
        //3.构造TurnoverReportVO对象并返回
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,",")) //[5-14，5-15，...5-20] --> "5-14,5-15,...5-20"
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStaticstics(LocalDate begin, LocalDate end) {
        //1.构造dateList数据
        List<LocalDate> dateList = getDateList(begin, end);

        //2.构造newUserList数据，新增用户列表
        List<Integer> newUserList = new ArrayList<>();

        //3.totalUserList数据，总用户列表
        List<Integer> totalUserList = new ArrayList<>();
        //循环遍历查询每日的新增用户数--user
        dateList.forEach(date -> {
            //select count(*) from user where create_time between #{} and #{}
            //select count(*) from user where create_time >= 当天开始时间 and create_time < 当天结束时间
            Map map = new HashMap<>();
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN)); //2024-05-14 00:00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));   //2024-05-14 23:59:59.999999
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);

            //循环遍历查询每日的总用户数--user
            //统计5-14日的注册用户数，就是要统计5-14日之前所有的用户数
            //select count(*) from user where create_time <= 当天结束时间
            map.put("beginTime", null); //2024-05-14 00:00:00
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
        });



        //4.构造UserReportVO对象并返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 获取指定日期范围内的日期列表数据
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<LocalDate>();
        //循环插入日期数据
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        log.info("dateList = {}" , dateList);

        return dateList;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        //1.获取日期列表数据 dateList
        List<LocalDate> dateList = getDateList(begin, end);


        //获取每日订单列表数据 orderCountList
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单列表数 validOrderCountList
        List<Integer> validOrderCountList = new ArrayList<>();
        //初始化订单总数
        Integer totalOrderCount = 0;
        //初始化有效订单总数
        Integer validOrderCount = 0;
        //2.获取每日订单列表数据 orderCountList
        // 统计每日orders表数量，条件：下单时间 >= 当天开始时间 and 下单时间 < 当天结束时间
        // 循环遍历日期列表进行订单统计

        for (LocalDate date : dateList) {
            //2.获取每日订单列表数据 orderCountList
            // 统计每日orders表数量，条件：下单时间 >= 当天开始时间 and 下单时间 < 当天结束时间
            Map map = new HashMap();
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));
            Integer totalOrder = orderMapper.countByMap(map);
            orderCountList.add(totalOrder);

            //3.每日有效订单列表数 validOrderCountList
            // 统计每日有效orders表数量，条件：状态=有效(已完成)、下单时间 >= 当天开始时间 and 下单时间 < 当天结束时间 and 订单状态 = 4
            map.put("status", Orders.COMPLETED);
            Integer validOrder = orderMapper.countByMap(map);
            validOrderCountList.add(validOrder);

            //4.获取订单总数 totalOrderCount
            // select count(*) from where 下单时间 >= '2025-05-14 00:00:00' and 下单时间 <= '2025-05-20 23:59:59.999999'
            // 将每日的订单数累加就是总订单数
//            totalOrderCount += totalOrder;

            //5.获取有效订单数 validOrderCount
            //将每日的有效订单数累加就是有效订单总数
            validOrderCount += validOrder;
        }

        //4.获取区间订单总数 totalOrderCount---写法二
//        for (Integer orderCount : orderCountList) {
//            totalOrderCount += orderCount;
//        }
        //[10,20,30,40,50,10,10]
//        totalOrderCount = orderCountList.stream().reduce(new BinaryOperator<Integer>() {
//            @Override
//            public Integer apply(Integer num1, Integer num2) { //累加器方法
//                return num1 + num2;
//            }
//        }).get();

        //Integer::sum 是integer提供的求和的方法
//        totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        totalOrderCount = orderCountList.stream().reduce(0,Integer::sum);





        //6.计算完成率 orderCompletionRate
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        //7.封装OrderReportVO对象并返回
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //1.构造nameList，商品名称列表
        List<String> nameList = new ArrayList<>();

        //2.构造numberList，商品销量(份数)列表
        List<Integer> numberList = new ArrayList<>();

        // 查询orders_detail + orders表，条件：订单状态-已完成，下单时间
        Map map = new HashMap();
        map.put("status", Orders.COMPLETED);
        map.put("beginTime", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("endTime", LocalDateTime.of(end, LocalTime.MAX));
        List<GoodsSalesDTO> list = orderMapper.sumTop10(map);

        for (GoodsSalesDTO dto : list) {
            nameList.add(dto.getName());
            numberList.add(dto.getNumber());
        }

        //3.封装SalesTop10ReportVO对象并返回
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }
}