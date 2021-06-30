package com.huateng.web.service;

import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.toprules.adapter.RulePkgWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/28
 * Time: 19:55
 * Description:
 */
@Slf4j
@Service
public class MemberPointsInitService {

    @Resource
    private RedisService redisService;
    @Resource
    private ApolloBean apolloBean;
    @Resource
    private RulePkgWorker rulePkgWorker;

    public void initRedis() {
        try {
            /**初始化错误码**/
            this.redisService.updateLocalCache(SysConstant.REDIS_ERROR_CODE, this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE_VALUE);
            /**初始化规则相关缓存**/
            this.redisService.updateLocalCache(SysConstant.REDIS_RULE_INFO_KEY, this.apolloBean.getServiceRuleKey(), SysConstant.REDIS_RULE_INFO_KEY_VALUE);
            /**初始化不能积分中类**/
            this.redisService.updateLocalCache(SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY, this.apolloBean.getNotProduceMidtypeKey(), SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY_VALUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void initRuleFile() {
        try {
            rulePkgWorker.getRuleFile();
            log.info("加载规则文件成功");
        }catch (Exception e){
            log.error("加载规则文件失败：{}", e.getMessage());
        }
    }
}
