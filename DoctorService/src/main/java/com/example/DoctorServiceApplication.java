package com.example;

// import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.example"})
public class DoctorServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(DoctorServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}