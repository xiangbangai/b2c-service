package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/7/31
 * Time: 17:06
 * Description: log服务初始化
 */
@Slf4j
@Service
public class LogInitService extends BaseService {

    @Resource
    private ApolloBean apolloBean;
    @Resource
    private RedisService redisService;

    /**
     * @User Sam
     * @Date 2020/8/3
     * @Time 21:24
     * @Param
     * @return
     * @Description 初始化数据
     */
    public void initRedis() {

        try {
            /**初始化错误码**/
            this.redisService.updateLocalCache(SysConstant.REDIS_ERROR_CODE, this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE_VALUE);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

}
