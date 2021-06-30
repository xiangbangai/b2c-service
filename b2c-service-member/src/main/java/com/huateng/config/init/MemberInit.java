package com.huateng.config.init;

import com.huateng.web.service.MemberInitService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class MemberInit {

    @Resource
    private MemberInitService memberInitService;

    @PostConstruct
    public void start(){
        //初始缓存
        this.memberInitService.initRedis();
    }

    @PreDestroy
    public void stop(){

    }
}