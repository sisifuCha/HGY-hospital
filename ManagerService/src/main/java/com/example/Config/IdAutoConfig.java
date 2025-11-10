package com.example.Config;

import com.example.utils.ScheduleIdGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdAutoConfig {

    // 使用CommandLineRunner在应用启动后初始化ScheduleId计数器
    @Bean
    public CommandLineRunner initializeId() {
        // 这里返回一个CommandLineRunner实现
        return args -> {
            ScheduleIdGenerator.initCounter();
        };
    }
}