package com.huateng.config.snowflakeidworker;

import com.huateng.common.util.SnowflakeIdWorker;
import com.huateng.config.apollo.ApolloYML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019-01-13
 * Time: 15:24
 * Description:
 */
@Configuration
public class SnowBeanConfig {

    @Value("${spring.cloud.client.ip-address}-${server.port}")
    private String snowKey;
    @Resource
    private ApolloYML apolloYML;



    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(this.apolloYML.getSnowMap().get(getSnowKey())[0], this.apolloYML.getSnowMap().get(getSnowKey())[1]);
    }

    public String getSnowKey() {
        return snowKey.replaceAll("\\.", "-");
    }
}
