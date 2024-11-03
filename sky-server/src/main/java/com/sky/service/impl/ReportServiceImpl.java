package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.file.WatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 统计指定时间区间内的营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //调用公共方法
        List<LocalDate> dateList=getLocalDateList(begin,end);
        /**
         * 查询每个日期对应的营业额
         * 状态：已完成
         */
        //存放每天营业额
        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date:dateList){
            //处理VOdate和数据库date兼容问题
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);//表示当前开始 00:00
            LocalDateTime endTime=LocalDateTime.of(date,LocalTime.MAX);//表示当天结束  23:59
            //构造Map
            Map map=new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            if (turnover == null){
                turnover=0.0;
            }

            turnoverList.add(turnover);
        }

        //使日期、营业额间以逗号分隔
        TurnoverReportVO turnoverReportVo = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();


        return turnoverReportVo;
    }

    /**
     * 统计指定时间区间的用户数量
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //调用公共方法
        List<LocalDate> dateList=getLocalDateList(begin,end);
        /**
         * 新增用户数量
         * 总用户数量
         */
        List<Integer> newUserList=new ArrayList<>();
        List<Integer> totalUserList=new ArrayList<>();

        for (LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map=new HashMap<>();
            //统计总用户数量
            map.put("end",endTime);
            Integer totalUser = userMapper.countByMap(map);
            //统计新用户数量
            map.put("begin",beginTime);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
       //封装VO对象
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();


        return userReportVO;
    }

    /**
     * 统计指定区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //调用公共方法
        List<LocalDate> dateList=getLocalDateList(begin,end);
        //订单总数集合和有效订单集合
        List<Integer> totalList=new ArrayList<>();
        List<Integer> validList=new ArrayList<>();
        //遍历datelist集合，查询相关订单数
        for (LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //获取订单总数
            Integer totalOrder = getOrderCount(beginTime, endTime, null);
            //获取有效订单
            Integer validOrder=getOrderCount(beginTime,endTime,Orders.COMPLETED);

            totalList.add(totalOrder);
            validList.add(validOrder);
        }

        //计算时间区间内订单总数量  使用Stream流
        Integer totalOrderCount = totalList.stream().reduce(Integer::sum).get();
        //计算时间区间内有效订单数量  使用Stream流
        Integer validOrderCount =validList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate=0.0;
        if (totalOrderCount != 0){
            orderCompletionRate= validOrderCount.doubleValue()/totalOrderCount.doubleValue();
        }
        //封装返回数据
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalList,","))
                .validOrderCountList(StringUtils.join(validList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();


        return orderReportVO;
    }

    /**
     * 统计指定区间的销量top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10List = orderMapper.getSalesTop10(beginTime, endTime);
        //提取name数据，修改为VO类型
        List<String> names = salesTop10List.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        //提取number数据，修改为VO类型
        List<Integer> numbers = salesTop10List.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .numberList(numberList)
                .nameList(nameList)
                .build();
        return salesTop10ReportVO;
    }
    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) throws IOException {
        //1.查询数据库，获得运营数据
        LocalDate begin=LocalDate.now().minusDays(30);
        LocalDate end=LocalDate.now().minusDays(1);

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //查询概览数据
        BusinessDataVO businessDatVO = workspaceService.getBusinessData(beginTime, endTime);

        //2.通过POI将数据写入Exccel文件中
        /**
         * 基于模板文件创建一个新的Excel文件
         */
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook excel=new XSSFWorkbook(resourceAsStream);
        //获取Excel报表Sheet页
        XSSFSheet sheet1 = excel.getSheet("Sheet1");
        //填充数据第二行--时间
        sheet1.getRow(1).getCell(1).setCellValue("时间："+begin+"至"+end);
        //获取第四行
        XSSFRow row4 = sheet1.getRow(3);
        row4.getCell(2).setCellValue(businessDatVO.getTurnover());
        row4.getCell(4).setCellValue(businessDatVO.getOrderCompletionRate());
        row4.getCell(6).setCellValue(businessDatVO.getNewUsers());
        //获取第五行
        XSSFRow row5 = sheet1.getRow(4);
        row5.getCell(2).setCellValue(businessDatVO.getValidOrderCount());
        row5.getCell(4).setCellValue(businessDatVO.getUnitPrice());
        /**
         * 填充明细数据
         */
        //填充30天明细数据
        for (int i=0;i<30;i++){
            LocalDate date=begin.plusDays(i);
            //查询某一天的营业数据
            BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
            //获得某一行
            XSSFRow row = sheet1.getRow(7 + i);
            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(3).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(5).setCellValue(businessData.getUnitPrice());
            row.getCell(6).setCellValue(businessData.getNewUsers());

        }
        //3.通过数据输出流将Excel文件下载到客户端
        ServletOutputStream outputStream = response.getOutputStream();
        excel.write(outputStream);
        //关闭资源
        outputStream.close();
        excel.close();
    }

    /**
     * 公共--根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        Integer result=orderMapper.countByMap(map);
        return result;
    }

    /**
     * 公共--根据动态日期获取日期列表
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getLocalDateList(LocalDate begin,LocalDate end){
        List<LocalDate> dateList=new ArrayList<>();
        //datelist循环加入日期
        while (!begin.equals(end)){
            dateList.add(begin);
            begin=begin.plusDays(1);
        }
        dateList.add(end);
        return dateList;
    }

}
