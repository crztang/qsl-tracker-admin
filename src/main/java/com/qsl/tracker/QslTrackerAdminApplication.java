package com.qsl.tracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.qsl.tracker.mapper")
@SpringBootApplication
public class QslTrackerAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(QslTrackerAdminApplication.class, args);
    }
}
