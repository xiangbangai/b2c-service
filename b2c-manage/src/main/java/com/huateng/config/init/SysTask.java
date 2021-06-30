package com.huateng.config.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Component
public class SysTask {

//    @Resource
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Resource
//    StringEncryptor stringEncryptor;
//    @Resource
//    private SnowflakeIdWorker snowflakeIdWorker;

    @PostConstruct
    public void start(){
    }

    @PreDestroy
    public void stop(){

    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void clearLoginInfo(){

    }
}
