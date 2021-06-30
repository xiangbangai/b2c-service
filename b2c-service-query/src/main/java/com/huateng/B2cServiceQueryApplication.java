package com.huateng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.**.config,com.**.controller,com.**.remote,com.**.service")
@EnableAutoConfiguration
@EnableFeignClients
public class B2cServiceQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2cServiceQueryApplication.class, args);
    }

}
