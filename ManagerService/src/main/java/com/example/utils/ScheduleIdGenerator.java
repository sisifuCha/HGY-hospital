package com.example.utils;

import com.example.Mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component; // 新增注解

import javax.annotation.PostConstruct; // 新增导入
import java.util.concurrent.atomic.AtomicInteger;

@Component // 添加此注解，让Spring管理这个类
public class ScheduleIdGenerator {
    @Autowired
    public ScheduleMapper scheduleMapper; // 现在可以被正常注入了

    private static final String PREFIX = "SCH";
    private static final int PADDING_LENGTH = 4;
    // 去除了静态修饰符，每个实例有自己的计数器（通常单例即可）
    private AtomicInteger counter = new AtomicInteger(1007);

    public String getNextId() { // 改为实例方法
        int nextNum = counter.incrementAndGet();
        return PREFIX + String.format("%0" + PADDING_LENGTH + "d", nextNum);
    }

    public void initCounter() { // 改为实例方法
        String maxId = scheduleMapper.getMaxId();
        if (maxId != null && maxId.startsWith(PREFIX) && maxId.length() > 3) {
            try {
                String numberPart = maxId.substring(3);
                int extractedValue = Integer.parseInt(numberPart);
                counter.set(extractedValue);
            } catch (NumberFormatException e) {
                System.err.println("数字转换错误: " + e.getMessage());
            }
        }
    }
}