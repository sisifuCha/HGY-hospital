package com.example.doctor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.doctor"})
public class DoctorServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(DoctorServiceApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}