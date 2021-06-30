package com.huateng.config.init;

import com.huateng.web.service.QueryInitRedisService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class QueryInit implements CommandLineRunner {

    @Resource
    private QueryInitRedisService queryInitRedisService;


    @Override
    public void run(String... args) throws Exception {
        this.queryInitRedisService.initRedis();
    }
}
