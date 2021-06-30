package com.huateng.config.snowflakeidworker;

import com.huateng.common.util.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SnowBeanConfig {

    @Value("${snowflakeidworker.workerId}")
    private long workerId;
    @Value("${snowflakeidworker.datacenterId}")
    private long datacenterId;


    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(workerId, datacenterId);
    }
}
