package com.huateng.config.init;

import com.huateng.web.service.LogInitService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


@Component
public class LogInit {

    @Resource
    private LogInitService logInitService;

    @PostConstruct
    public void start(){
        //初始化错误码缓存
        this.logInitService.initRedis();
    }
}
