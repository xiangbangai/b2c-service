package com.huateng.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/10/18
 * Time: 09:42
 * Description:
 */
@Slf4j
@Service
public class QueryInitRedisService {

    @Resource
    private CoreInitService coreInitService;

    public void initRedis() throws Exception {
        //初始化错误码缓存
        this.coreInitService.initErrorInfo();
        //初始化字典
        this.coreInitService.initDict();
        //初始化渠道
        this.coreInitService.initChannel();
        //初始化规则相关缓存
        this.coreInitService.initRuleInfo();
        //初始化不能积分的商品中类
        this.coreInitService.notProduceMidtype();
        //初始化服务信息
        this.coreInitService.initServiceInfo();
    }
}
