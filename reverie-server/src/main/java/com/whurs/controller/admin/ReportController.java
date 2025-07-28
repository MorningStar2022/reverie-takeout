package com.whurs.controller.admin;

import com.whurs.result.Result;
import com.whurs.service.ReportService;
import com.whurs.vo.OrderReportVO;
import com.whurs.vo.SalesTop10ReportVO;
import com.whurs.vo.TurnoverReportVO;
import com.whurs.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.HttpResource;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
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
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额统计:{},{}",begin,end);
        TurnoverReportVO turnoverReportVO=reportService.getTurnoverStatistics(begin,end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户数量统计，包括新增用户与总用户
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数量统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数量统计:{},{}",begin,end);
        UserReportVO userReportVO=reportService.getUserStatistics(begin,end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计:{},{}",begin,end);
        OrderReportVO orderReportVO=reportService.getOrdersStatistics(begin,end);
        return Result.success(orderReportVO);
    }

    /**
     * 销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("top10统计")
    public Result<SalesTop10ReportVO> salesTop10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("销量top10统计:{},{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO=reportService.getSalesTop10(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 导出运营数据报表
     * @param httpServletResponse
     */
    @GetMapping("/export")
    @ApiOperation("导出运营报表")
    public void exportExcel(HttpServletResponse httpServletResponse){
        reportService.exportBusinessData(httpServletResponse);
    }
}
