package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
     * 统计指定时间区间内的营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合存放从begin到end范围每天的日期
        List<LocalDate> dateList=new ArrayList<>();
        //日期循环加入
        while (!begin.equals(end)){
            dateList.add(begin);
            begin=begin.plusDays(1);
        }
        dateList.add(end);
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
        List<LocalDate> dateList=new ArrayList<>();
        //datelist循环加入日期
        while (!begin.equals(end)){
            dateList.add(begin);
            begin=begin.plusDays(1);
        }
        dateList.add(end);
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
}
