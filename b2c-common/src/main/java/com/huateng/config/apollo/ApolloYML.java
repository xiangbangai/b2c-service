package com.huateng.config.apollo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/15
 * Time: 15:34
 * Description:
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "b2c.yml")
public class ApolloYML {

    private Map<String,long[]> snowMap;
    private Map<String, Double> userLevel;
    private Map<Integer, String> noPointsList;//指定商品不能积分列表
}
