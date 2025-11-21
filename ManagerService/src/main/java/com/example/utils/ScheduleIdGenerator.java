package com.example.utils;

import com.example.Mapper.ScheduleMapper;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component; // 新增注解

import javax.annotation.PostConstruct; // 新增导入
import java.util.ArrayList;
import java.util.List;
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
        //获取所有的ID
        List<String> IdList = scheduleMapper.getIdList();
        //获取所有ID的数字部分
        List<Integer> IdValueList = new ArrayList<>();
        for(String Id:IdList) {
            String value = Id.substring(3);
            int extractValue = Integer.parseInt(value);
            IdValueList.add(extractValue);
        }
        Integer max = 0;
        for (Integer value:IdValueList) {
            if (value>max){max=value;}
        }
        counter.set(max);
    }
}