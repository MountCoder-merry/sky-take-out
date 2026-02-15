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

@Api(tags = "数据统计相关接口")
@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {
 
    @Autowired
    private ReportService reportService;
 
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("营业额统计")
    @GetMapping("/turnoverStatistics")
    public Result turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额统计：{},{}",begin,end);
        TurnoverReportVO vo = reportService.turnoverStatistics(begin,end);
        return Result.success(vo);
    }

    @ApiOperation("用户统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        log.info("用户统计：{},{}",begin,end);
        UserReportVO vo = reportService.userStaticstics(begin,end);
        return Result.success(vo);
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("订单统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("订单统计：{},{}",begin,end);
        OrderReportVO vo = reportService.orderStatistics(begin,end);
        return Result.success(vo);
    }

    /**
     * 销量top10
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("销量top10")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("销量top10：{},{}",begin,end);
        SalesTop10ReportVO vo = reportService.top10(begin,end);
        return Result.success(vo);
    }
}