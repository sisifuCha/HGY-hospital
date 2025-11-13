package com.example.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleIdGenerator {
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

    // 可选：在应用启动时，从数据库加载当前最大的ID来初始化计数器
    public static void initCounter(int initialValue) {
        counter.set(initialValue);
    }
}