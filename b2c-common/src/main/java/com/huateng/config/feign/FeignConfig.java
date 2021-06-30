package com.huateng.config.feign;

import com.huateng.config.apollo.ApolloBean;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/13
 * Time: 22:15
 * Description:
 */
@Configuration
public class FeignConfig {

    @Resource
    private ApolloBean apolloBean;

    @Bean
    public Request.Options options() {
        return new Request.Options(this.apolloBean.getFeignTimeout(), this.apolloBean.getFeignTimeout());
    }
}
