package com.huateng.config.log;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogStashDealUtils {

    private LogStashDealUtils(){

    }

    public static void log(String msg){
        log.info("{}",msg);
    }
}
