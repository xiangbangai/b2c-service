package com.huateng.config.apollo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/15
 * Time: 21:29
 * Description:
 */
@Data
@RefreshScope
@Component
public class ApolloBean {

    @Value("${b2c.service.global.error.key}")
    private String serviceErrorKey; //serviceErrorKey
    @Value("${b2c.service.global.dict.key}")
    private String serviceDictKey; //serviceDictKey
    @Value("${b2c.service.global.channel.key}")
    private String serviceChannelKey;
    @Value("${b2c.service.global.check.rule}")
    private String serviceRuleKey;
    @Value("${b2c.service.global.interface.key}")
    private String interfaceKey;
    @Value("${b2c.service.feign.timeout}")
    private int feignTimeout; //feign超时时间
    @Value("${b2c.service.token.timeout}")
    private int tokenTimeout; //token超时时间
    @Value("${b2c.service.wechat.qrcode.timeout}")
    private int wechatTimeOut; //微信二维码过期时间
    @Value("${b2c.service.wechat.qrcode.key}")
    private int wechatKey; //微信二维码key
    @Value("${b2c.service.redisson.time.wait}")
    private long redissonTimeWait;
    @Value("${b2c.service.redisson.time.expired}")
    private long redissonTimeExpired;
    @Value("${b2c.service.global.nopoints.midtype}")
    private String notProduceMidtypeKey;
}
