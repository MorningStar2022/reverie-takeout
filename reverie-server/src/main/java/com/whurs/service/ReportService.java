package com.whurs.service;

import com.whurs.vo.OrderReportVO;
import com.whurs.vo.SalesTop10ReportVO;
import com.whurs.vo.TurnoverReportVO;
import com.whurs.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 营业额统计
     * @param start
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end);

    /**
     * 统计当天新增与总用户数量
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 查询销量top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     * @param httpServletResponse
     */
    void exportBusinessData(HttpServletResponse httpServletResponse);

}
