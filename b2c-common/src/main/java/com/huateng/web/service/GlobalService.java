package com.huateng.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.model.db2.ServiceChannel;
import com.huateng.data.model.db2.ServiceDict;
import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.data.model.db2.ServiceInterfaceInfo;
import com.huateng.data.model.db2.ServiceNotProduceMidtype;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/10/17
 * Time: 12:55
 * Description:
 */
@Service
public class GlobalService {

    @Resource
    private RedisService redisService;
    @Resource
    private ApolloBean apolloBean;

    /**
     * 获取字典
     * @return
     * @throws Exception
     */
    public Map<String,Map<String,ServiceDict>> getDict() throws Exception {
        this.redisService.updateLocalCache(SysConstant.REDIS_DICT_CODE, this.apolloBean.getServiceDictKey(), SysConstant.REDIS_DICT_CODE_VALUE);
        String json = redisService.redisMap.get(SysConstant.REDIS_DICT_CODE_VALUE);
        return JacksonUtil.toObject(json, new TypeReference<Map<String,Map<String,ServiceDict>>>() {});
    }

    /**
     * 获取渠道
     * @return
     * @throws Exception
     */
    public Map<String, ServiceChannel> getChannel() throws Exception {
        this.redisService.updateLocalCache(SysConstant.REDIS_CHANNEL_KEY, this.apolloBean.getServiceChannelKey(), SysConstant.REDIS_CHANNEL_KEY_VALUE);
        String json = redisService.redisMap.get(SysConstant.REDIS_CHANNEL_KEY_VALUE);
        return JacksonUtil.toObject(json, new TypeReference<Map<String, ServiceChannel>>() {});
    }

    /**
     * 获取错误码对象
     * @param errorCode
     * @return
     * @throws Exception
     */
    public ServiceErrorInfo getErrorInfo(String errorCode) throws Exception{
        return this.redisService.getErrorInfo().get(errorCode);
    }

    /**
     * 获取规则相关缓存
     * @return
     * @throws Exception
     */
    public Map<String, Object> getRuleInfo() throws Exception {
        this.redisService.updateLocalCache(SysConstant.REDIS_RULE_INFO_KEY, this.apolloBean.getServiceRuleKey(), SysConstant.REDIS_RULE_INFO_KEY_VALUE);
        String json = redisService.redisMap.get(SysConstant.REDIS_RULE_INFO_KEY_VALUE);
        return JacksonUtil.toObject(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 获取不能积分中类列表
     * @return
     * @throws Exception
     */
    public Map<Integer, ServiceNotProduceMidtype> getNotProduceMidtypeMap() throws Exception {
        this.redisService.updateLocalCache(SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY, this.apolloBean.getNotProduceMidtypeKey(), SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY_VALUE);
        String json = redisService.redisMap.get(SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY_VALUE);
        return JacksonUtil.toObject(json, new TypeReference<Map<Integer, ServiceNotProduceMidtype>>() {});
    }

    /**
     * 获取服务信息缓存
     * @return
     * @throws Exception
     */
    public Map<String, ServiceInterfaceInfo> getServiceInfo() throws Exception {
        this.redisService.updateLocalCache(SysConstant.REDIS_INTERFACE_KEY, this.apolloBean.getInterfaceKey(), SysConstant.REDIS_INTERFACE_KEY_VALUE);
        String json = redisService.redisMap.get(SysConstant.REDIS_INTERFACE_KEY_VALUE);
        return JacksonUtil.toObject(json, new TypeReference<Map<String, ServiceInterfaceInfo>>() {});
    }

}
