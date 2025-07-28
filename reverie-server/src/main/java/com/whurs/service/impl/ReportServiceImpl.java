package com.whurs.service.impl;

import com.whurs.dto.GoodsSalesDTO;
import com.whurs.entity.Orders;
import com.whurs.mapper.OrderMapper;
import com.whurs.mapper.UserMapper;
import com.whurs.service.ReportService;
import com.whurs.service.WorkspaceService;
import com.whurs.vo.*;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * @param start
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end) {
        //定义一个集合存储从start到end所有日期
        List<LocalDate> localDateList=new ArrayList<>();
        localDateList.add(start);
        while(!start.equals(end)){
            start=start.plusDays(1);
            localDateList.add(start);
        }
        String dateList = StringUtils.join(localDateList, ",");
        //定义一个集合存储每日营业额
        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date : localDateList) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("start",startTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.getTurnoverByMap(map);
            turnover=turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }
        String turnoverStrList = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverStrList)
                .build();

    }

    /**
     * 统计当天新增与总用户数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //定义一个集合存储从start到end所有日期
        List<LocalDate> localDateList=new ArrayList<>();
        localDateList.add(begin);
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            localDateList.add(begin);
        }
        String dateList = StringUtils.join(localDateList, ",");

        //存放每天新增用户数量
        List<Integer> newUserList=new ArrayList<>();
        //存放每天总用户数量
        List<Integer> totalUserList=new ArrayList<>();
        for (LocalDate date : localDateList) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("end",endTime);
            Integer totalUserNum=userMapper.countByMap(map);
            totalUserList.add(totalUserNum);
            map.put("begin",startTime);
            Integer newUserNum=userMapper.countByMap(map);
            newUserList.add(newUserNum);
        }
        String newUserStrList = StringUtils.join(newUserList, ",");
        String totalUserStrList = StringUtils.join(totalUserList, ",");
        return UserReportVO.builder()
                .dateList(dateList)
                .newUserList(newUserStrList)
                .totalUserList(totalUserStrList)
                .build();

    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //定义一个集合存储从start到end所有日期
        List<LocalDate> localDateList=new ArrayList<>();
        localDateList.add(begin);
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            localDateList.add(begin);
        }
        List<Integer> validOrderList=new ArrayList<>();
        List<Integer> totalOrderList=new ArrayList<>();
        for (LocalDate date : localDateList) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer validOrderCount=getOrderCount(startTime,endTime,Orders.COMPLETED);
            Integer totalOrderCount=getOrderCount(startTime,endTime,null);
            validOrderList.add(validOrderCount);
            totalOrderList.add(totalOrderCount);
        }
        Integer validOrderSum=validOrderList.stream().reduce(Integer::sum).get();
        Integer totalOrderSum=totalOrderList.stream().reduce(Integer::sum).get();
        Double orderCompleteRate=totalOrderSum!=0?validOrderSum.doubleValue()/totalOrderSum:0.0;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDateList, ","))
                .orderCountList(StringUtils.join(totalOrderList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(totalOrderSum)
                .validOrderCount(validOrderSum)
                .orderCompletionRate(orderCompleteRate).build();

    }

    /**
     * 统计销量top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime startTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOS= orderMapper.getSalesTop10(startTime,endTime);
        List<String> names = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }

    /**
     * 导出运营报表
     * @param httpServletResponse
     */
    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        //查询最近30天的营业数据
        LocalDate dateBegin=LocalDate.now().minusDays(30);
        LocalDate dateEnd=LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //获取输入流对象
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("时间:"+dateBegin+"至"+dateEnd);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date=dateBegin.plusDays(i);
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row=sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            //通过输出流将excel下载到客户端浏览器
            ServletOutputStream os = httpServletResponse.getOutputStream();
            excel.write(os);
            os.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map=new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }
}
