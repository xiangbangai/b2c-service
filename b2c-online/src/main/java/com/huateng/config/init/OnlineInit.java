package com.huateng.config.init;

import com.huateng.web.service.OnlineInitService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;


@Component
public class OnlineInit {

    @Resource
    private OnlineInitService onlineInitService;

    @PostConstruct
    public void start(){
        //初始化错误码缓存
        this.onlineInitService.initRedis();
    }

    @PreDestroy
    public void stop(){

    }

}
