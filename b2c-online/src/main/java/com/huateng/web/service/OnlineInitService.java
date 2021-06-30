package com.huateng.web.service;

import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/15
 * Time: 23:20
 * Description:
 */
@Slf4j
@Service
public class OnlineInitService {

    @Resource
    private ApolloBean apolloBean;
    @Resource
    private RedisService redisService;

    /**
     * @User Sam
     * @Date 2019/9/16
     * @Time 18:23
     * @Param 
     * @return 
     * @Description 初始redis
     */
    public void initRedis() {

        try {
            /**初始化错误码**/
            this.redisService.updateLocalCache(SysConstant.REDIS_ERROR_CODE, this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE_VALUE);
            /**初始化字典**/
            this.redisService.updateLocalCache(SysConstant.REDIS_DICT_CODE, this.apolloBean.getServiceDictKey(), SysConstant.REDIS_DICT_CODE_VALUE);
            /**初始化渠道**/
            this.redisService.updateLocalCache(SysConstant.REDIS_CHANNEL_KEY, this.apolloBean.getServiceChannelKey(), SysConstant.REDIS_CHANNEL_KEY_VALUE);
            /**初始化服务信息**/
            this.redisService.updateLocalCache(SysConstant.REDIS_INTERFACE_KEY, this.apolloBean.getInterfaceKey(), SysConstant.REDIS_INTERFACE_KEY_VALUE);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

}
