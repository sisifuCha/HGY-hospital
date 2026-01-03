package com.example;

// import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.example"})
@EnableScheduling
public class DoctorServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(DoctorServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}