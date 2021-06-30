package com.huateng.config.apollo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
public class ApolloControl {

    @Value("${b2c.control.many.oil}")
    private Integer manyOil; //多油品
    @Value("${b2c.control.manual.oil}")
    private Integer manualWorkOil; //手工录入油品
    @Value("${b2c.control.limit.day}")
    private Integer limitDay; //日累计
    @Value("${b2c.control.limit.day.count}")
    private BigDecimal limitDayCount; //日累计次数
    @Value("${b2c.control.log}")
    private Short logStatus; //日志状态
    @Value("${b2c.service.redisson.time.wait}")
    private Long redissonTimeWait;
    @Value("${b2c.service.redisson.time.expired}")
    private Long redissonTimeExpired;

}
