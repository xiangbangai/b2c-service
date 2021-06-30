package com.huateng.web.service;

import com.dianping.cat.Cat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloBean;
import com.huateng.data.model.db2.ServiceErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RTransaction;
import org.redisson.api.RedissonClient;
import org.redisson.api.TransactionOptions;
import org.redisson.client.codec.StringCodec;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/15
 * Time: 17:48
 * Description:
 */
@Slf4j
@Service
public class RedisService {

    /**本地rediskey**/
    public Map<String, String> redisMap = new ConcurrentHashMap<>();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ApolloBean apolloBean;

    private final StampedLock stampedLock = new StampedLock();


    /**
     * 启动初始化缓存，只能启动时调用，后续更新不可调用
     * @param primaryKey 主key
     * @param json 本地key
     * @param json 值
     */
    public void saveData(String primaryKey, String localKey, String json, String localValueKey) {
        String redisKey = primaryKey + "." + System.currentTimeMillis();
        set(redisKey, json);
        if(setIfAbsent(primaryKey, redisKey)) {
            redisMap.put(localKey, redisKey);
            redisMap.put(localValueKey, json);
        } else {
            String _redisKey = get(primaryKey);
            redisMap.put(localKey, _redisKey);
            redisMap.put(localValueKey, get(_redisKey));
            delete(redisKey);
        }
    }

    /**
     * 主动刷新本地缓存
     * @param primaryKey apollo中的key
     * @param localKey 常量类中本地key
     * @param data 数据
     * @param localValueKey 本地valueKey
     * @param timeout 过期时间
     * @param unit 单位
     * @param delKey 删除原来的key
     */
    public void saveData(String primaryKey, String localKey, String data, String localValueKey,long timeout, TimeUnit unit, String delKey ) {
        String redisKey = primaryKey + SysConstant.SPLIT_TYPE3 + System.currentTimeMillis();
        RTransaction rTransaction = this.redissonClient.createTransaction(TransactionOptions.defaults());
        RBucket<String> rBucket1 = rTransaction.getBucket(redisKey, StringCodec.INSTANCE);
        RBucket<String> rBucket2 = rTransaction.getBucket(primaryKey, StringCodec.INSTANCE);
        RBucket<String> rBucket3;
        try {
            rBucket1.set(data, timeout, unit);
            rBucket2.set(redisKey);
            if (delKey != null && !"".equals(delKey)) {
                rBucket3 = rTransaction.getBucket(delKey, StringCodec.INSTANCE);
                rBucket3.delete();
            }
            redisMap.put(localKey, redisKey);
            redisMap.put(localValueKey, data);
            rTransaction.commit();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            rTransaction.rollback();
            throw e;
        }

    }

    /**
     * 获取错误码对象
     * @return
     * @throws Exception
     */
    public Map<String, ServiceErrorInfo> getErrorInfo() throws Exception{
        updateLocalCache(SysConstant.REDIS_ERROR_CODE, this.apolloBean.getServiceErrorKey(), SysConstant.REDIS_ERROR_CODE_VALUE);
        String json = redisMap.get(SysConstant.REDIS_ERROR_CODE_VALUE);
        return JacksonUtil.toObject(json, new TypeReference<Map<String,ServiceErrorInfo>>() {});
    }

    /**
     * @User Sam
     * @Date 2019/9/16
     * @Time 23:38
     * @Param
     * @return
     * @Description 检查本地缓存并更新
     */
    public void updateLocalCache(String localKey, String primaryKey, String localValueKey) {
        String redisKey = "";
        if (redisMap.containsKey(localKey)) {
            //本地存在key
            String _tempkey = redisMap.get(localKey);
            //远程验证
            /**验证后即可使用本地缓存，减少网络传输**/
            try {
                if (hasKey(_tempkey)) {
                    redisKey = _tempkey;
                }
            } catch (Exception e) {
                log.error("Redis判断hasKey连接异常...", e);
                Cat.logError("Redis判断hasKey连接异常...",e);
            }
        }
        /**第一次或者数据过期进入**/
        if ("".equals(redisKey)) {
            //需要从远程获取
            try {
                if (hasKey(primaryKey)) {
                    redisKey = get(primaryKey);
                    /**使用锁**/
                    long stamp = stampedLock.writeLock();
                    try {
                        redisMap.put(localKey, redisKey);
                        redisMap.put(localValueKey, get(redisKey));
                    } finally {
                        /**释放锁**/
                        stampedLock.unlockWrite(stamp);
                    }
                }
            } catch (Exception e) {
                log.error("Redis同步本地缓存异常...", e);
                Cat.logError("Redis同步本地缓存异常...",e);
            }
        }

        //redis异常
        if (!redisMap.containsKey(localKey) && ("".equals(redisKey) || null == redisKey)) {
            ServiceErrorInfo serviceErrorInfo = new ServiceErrorInfo();
            serviceErrorInfo.setErrorCode(SysConstant.SYS_REDIS_ERROR);
            serviceErrorInfo.setErrorMsg(SysConstant.SYS_REDIS_ERROR_MSG);
            log.error("Redis异常，本地无缓存远程无法同步...");
            throw new BusinessException(serviceErrorInfo);
        }
    }




    /**redis操作方法**/


    /**
     * 删除key
     *
     * @param key
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 是否存在key
     *
     * @param key
     * @return
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置指定 key 的值
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置key和过期时间
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置key和过期时间
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        RBucket<String> rBucket = this.redissonClient.getBucket(key, StringCodec.INSTANCE);
        return rBucket.trySet(value, timeout, unit);
    }



    /**
     * 只有在 key 不存在时设置 key 的值
     *
     * @param key
     * @param value
     * @return 之前已经存在返回false,不存在返回true
     */
    public boolean setIfAbsent(String key, String value) {
        RBucket<String> rBucket = this.redissonClient.getBucket(key, StringCodec.INSTANCE);
        return rBucket.trySet(value);
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key
     * @param unit
     * @return
     */
    public Long getExpire(String key, TimeUnit unit) {
        return stringRedisTemplate.getExpire(key, unit);
    }

    /**
     * 获取指定 key 的值
     * @param key
     * @return
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
     *
     * @param key
     * @param value
     * @return
     */
    public String getAndSet(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }
}
