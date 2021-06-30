package com.huateng.config.init;

import com.huateng.web.service.MemberPointsInitService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/28
 * Time: 19:54
 * Description:
 */
@Component
public class MemberPointsInit {

    @Resource
    private MemberPointsInitService memberPointsInitService;

    @PostConstruct
    public void start(){
        //初始缓存
        this.memberPointsInitService.initRedis();
        //加载规则文件
        this.memberPointsInitService.initRuleFile();
    }

    @PreDestroy
    public void stop(){

    }
}
