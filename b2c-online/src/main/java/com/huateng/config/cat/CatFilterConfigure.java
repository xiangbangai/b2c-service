package com.huateng.config.cat;

import com.dianping.cat.servlet.CatFilter;
import com.huateng.common.util.SysConstant;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CatFilterConfigure {

    @Bean
    public FilterRegistrationBean<CatFilter> catFilter() {
        FilterRegistrationBean<CatFilter> registration = new FilterRegistrationBean<>(new CatFilter());
        registration.addUrlPatterns(SysConstant.FILTER_PATTERNS);
        registration.setName(SysConstant.FILTER_NAME);
        registration.setOrder(1);
        return registration;
    }
}
