package com.huateng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = "com.**.config,com.**.controller,com.**.remote,com.**.service")
@EnableScheduling
@EnableAutoConfiguration
@EnableFeignClients
public class B2cServiceLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2cServiceLogApplication.class, args);
    }

}
