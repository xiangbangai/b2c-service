package com.huateng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(basePackages = "com.**.config,com.**.controller,com.**.remote,com.**.service")
@EnableAutoConfiguration
@EnableFeignClients
@EnableAsync
public class B2cMemberPointsApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2cMemberPointsApplication.class, args);
    }

}
