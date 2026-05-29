package com.videoaggregator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.videoaggregator.mapper")
public class VideoAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoAggregatorApplication.class, args);
    }
}
