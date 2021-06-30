package com.huateng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;




@ComponentScan(basePackages = "com.**.config,com.**.controller,com.**.service")
@EnableScheduling
@EnableAutoConfiguration
public class B2cManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2cManageApplication.class, args);
    }

}
