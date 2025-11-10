package com.example.utils;

import com.example.Mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
public class ScheduleIdGenerator {
    @Autowired
    private static ScheduleMapper scheduleMapper;
    // 定义前缀和数字格式
    private static final String PREFIX = "SCH";
    private static final int PADDING_LENGTH = 4; // 数字部分位数，如1008 -> 1008, 1009
    private static AtomicInteger counter = new AtomicInteger(1007); // 从1007开始自增

    // 生成下一个ID
    public static String getNextId() {
        int nextNum = counter.incrementAndGet(); // 线程安全地自增1
        // 将数字部分格式化为指定位数，不足位补0
        return PREFIX + String.format("%0" + PADDING_LENGTH + "d", nextNum);
    }

    // 在应用启动时，从数据库加载当前最大的ID来初始化计数器
    public static void initCounter() {
        String maxId = scheduleMapper.getMaxId();
        if (maxId != null && maxId.startsWith("SCH") && maxId.length() > 3) {
            try {
                String numberPart = maxId.substring(3); // 去掉"SCH"前缀
                int extractedValue = Integer.parseInt(numberPart);
                counter.set(extractedValue);
                return;
            } catch (NumberFormatException e) {
                System.err.println("数字转换错误: " + e.getMessage());
            }
        }
    }
}