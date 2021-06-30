package com.huateng.web.service;

import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.db2.mapper.core.*;
import com.huateng.data.model.db2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huateng.data.model.db2.ServiceChannel;
import com.huateng.data.model.db2.ServiceDict;
import com.huateng.data.model.db2.ServiceErrorInfo;
import com.huateng.data.model.db2.ServiceInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/15
 * Time: 23:20
 * Description:
 */
@Slf4j
@Service
public class CoreInitService {

    @Resource
    private RedisService redisService;
    @Resource
    private ApolloBean apolloBean;
    @Resource
    private ServiceDictMapper coreServiceDictMapper;
    @Resource
    private ServiceErrorInfoMapper coreServiceErrorInfoMapper;
    @Resource
    private ServiceChannelMapper coreserviceChannelMapper;
    @Resource
    private TblSpecialRuleMapper coreTblSpecialRuleMapper;
    @Resource
    private ServiceNotProduceMidtypeMapper serviceNotProduceMidtypeMapper;
    @Resource
    private ServiceInterfaceInfoMapper serviceInterfaceInfoMapper;




    /**
     * @User Sam
     * @Date 2019/10/17
     * @Time 19:17
     * @Param
     * @return
     * @Description 初始化Redis
     */
    public boolean initLocalCache(String remoteKey, String localKey, String localValueKey) {
        if(this.redisService.hasKey(remoteKey)) {
            String redisKey = this.redisService.get(remoteKey);
            this.redisService.redisMap.put(localKey, redisKey);
            this.redisService.redisMap.put(localValueKey, this.redisService.get(redisKey));
            return true;
        } else {
            return false;
        }
    }
    /**
     * @User Sam
     * @Date 2019/9/15
     * @Time 23:22
     * @Param
     * @return
     * @Description 初始化错误码缓存
     */
    public void initErrorInfo() throws Exception {
        if(!initLocalCache(this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE, SysConstant.REDIS_ERROR_CODE_VALUE)) {
            Map<String, ServiceErrorInfo> map = this.coreServiceErrorInfoMapper.getAll().stream().collect(
                    Collectors.toMap(ServiceErrorInfo::getErrorCode, Function.identity()));
            this.redisService.saveData(this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE, JacksonUtil.toJson(map), SysConstant.REDIS_ERROR_CODE_VALUE);
        }
        log.info("错误码初始化完毕...");
    }



    /**
     * @User Sam
     * @Date 2019/10/17
     * @Time 12:01
     * @Param
     * @return
     * @Description 初始化字典缓存
     */
    public void initDict() throws Exception {
        if(!initLocalCache(this.apolloBean.getServiceDictKey(), SysConstant.REDIS_DICT_CODE, SysConstant.REDIS_DICT_CODE_VALUE)) {
            Map<String, Map<String, ServiceDict>> map = getDict(this.coreServiceDictMapper.getDict());
            this.redisService.saveData(this.apolloBean.getServiceDictKey(), SysConstant.REDIS_DICT_CODE, JacksonUtil.toJson(map), SysConstant.REDIS_DICT_CODE_VALUE);
        }
        log.info("字典初始化完毕...");
    }

    /**
     * @User Sam
     * @Date 2019/10/17
     * @Time 19:01
     * @Param
     * @return
     * @Description 初始化渠道信息
     */
    public void initChannel() throws Exception {
        if(!initLocalCache(this.apolloBean.getServiceChannelKey(), SysConstant.REDIS_CHANNEL_KEY, SysConstant.REDIS_CHANNEL_KEY_VALUE)) {
            Map<String, ServiceChannel> map = this.coreserviceChannelMapper.getChannel().stream().collect(Collectors.toMap(ServiceChannel::getId, Function.identity()));
            this.redisService.saveData(this.apolloBean.getServiceChannelKey(), SysConstant.REDIS_CHANNEL_KEY, JacksonUtil.toJson(map), SysConstant.REDIS_CHANNEL_KEY_VALUE);
        }
        log.info("渠道初始化完毕...");
    }

    /**
     * @User Sam
     * @Date 2020/3/28
     * @Time 17:09
     * @Param
     * @return
     * @Description 初始化规则相关缓存
     */
    public void initRuleInfo() throws Exception {
        if(!initLocalCache(this.apolloBean.getServiceRuleKey(), SysConstant.REDIS_RULE_INFO_KEY, SysConstant.REDIS_RULE_INFO_KEY_VALUE)) {
            Map<String, Object> ruleMap = new HashMap<>();
            ruleMap.put(SysConstant.DICT_KEY_RULE_CARD_BIN, this.coreTblSpecialRuleMapper.getCardBin());//自定义卡bin
            this.redisService.saveData(this.apolloBean.getServiceRuleKey(), SysConstant.REDIS_RULE_INFO_KEY, JacksonUtil.toJson(ruleMap), SysConstant.REDIS_RULE_INFO_KEY_VALUE);
        }
        log.info("规则相关缓存初始化完毕...");
    }

    /**
     * @User Sam
     * @Date 2020/8/5
     * @Time 13:01
     * @Param
     * @return
     * @Description 初始化服务信息
     */
    public void initServiceInfo() throws Exception {
        if(!initLocalCache(this.apolloBean.getInterfaceKey(), SysConstant.REDIS_INTERFACE_KEY, SysConstant.REDIS_INTERFACE_KEY_VALUE)) {
            Map<String, ServiceInterfaceInfo> map = this.serviceInterfaceInfoMapper.getList().stream().collect(Collectors.toMap(ServiceInterfaceInfo::getUrl, Function.identity()));
            this.redisService.saveData(this.apolloBean.getInterfaceKey(), SysConstant.REDIS_INTERFACE_KEY, JacksonUtil.toJson(map), SysConstant.REDIS_INTERFACE_KEY_VALUE);
        }
        log.info("服务信息初始化完毕...");
    }

    /**
     * 获取字典
     * @return
     */
    public Map<String, Map<String, ServiceDict>> getDict(List<ServiceDict> list) {
        Map<String, Map<String, ServiceDict>> map = new HashMap<>();
        Map<String, ServiceDict> dictMap = null;
        String id = "";
        ServiceDict serviceDict;
        for (int i = 0; i < list.size(); i++) {
            serviceDict = list.get(i);
            if (serviceDict.getDictLevel() == 1) {
                if (i != 0) {
                    map.put(id, dictMap);
                }
                id = serviceDict.getId();
                dictMap = new HashMap<>();
            } else {
                dictMap.put(serviceDict.getDictKey(), serviceDict);
            }
            if (i == list.size() - 1) {
                map.put(id, dictMap);
            }
        }
        return map;
    }

    /**
     * 初始化不能积分商品中类
     */
    public void notProduceMidtype() throws Exception{
        if(!initLocalCache(this.apolloBean.getNotProduceMidtypeKey(), SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY, SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY_VALUE)) {
            Map<Integer, ServiceNotProduceMidtype> map = this.serviceNotProduceMidtypeMapper.getAllServiceNotProduceMidtype().stream().collect(Collectors.toMap(ServiceNotProduceMidtype::getMiddleType, Function.identity()));

            this.redisService.saveData(this.apolloBean.getNotProduceMidtypeKey(), SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY, JacksonUtil.toJson(map), SysConstant.REDIS_NOT_PRODUCE_MIDTYPE_KEY_VALUE);
        }
        log.info("不能积分商品中类初始化完毕...");
    }
}
