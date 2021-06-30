package com.huateng.web.service;

import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class MemberInitService {

    @Resource
    private RedisService redisService;
    @Resource
    private ApolloBean apolloBean;

    public void initRedis() {
        try {
            /**初始化错误码**/
            this.redisService.updateLocalCache(SysConstant.REDIS_ERROR_CODE, this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE_VALUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
