package com.clarimire;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.clarimire.mapper")
public class ClarimireApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClarimireApplication.class, args);
    }
} 