package com.huateng.config.redisson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huateng.common.util.SysConstant;
import org.jasypt.encryption.StringEncryptor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/9/1
 * Time: 09:59
 * Description:
 */
@Configuration
public class RedissonConfig {

    @Resource
    StringEncryptor stringEncryptor;

    @Value("${spring.profiles.include}")
    private String env;

    @Bean
    RedissonClient redissonClient() throws IOException {
        String fileName = MessageFormat.format(SysConstant.REDISSON_CONFIG_FILE_NAME, env);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(
                new BufferedReader(
                        new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(fileName))));
        String configName;
        switch (env) {
            case "test":
            case "uat":
            case "pro":
                configName = SysConstant.REDISSON_CONFIG_CLUSTER;
                break;
            default:
                configName = SysConstant.REDISSON_CONFIG_SINGLE;
        }
        ObjectNode objectNode  = (ObjectNode) jsonNode.findPath(configName);
        String pwd = jsonNode.findPath(SysConstant.REDISSON_UPATE_PASSWORD).textValue();
        if (pwd != null && !"".equals(pwd)) {
            objectNode.put(SysConstant.REDISSON_UPATE_PASSWORD, stringEncryptor.decrypt(pwd));
        } else {
            objectNode.remove(SysConstant.REDISSON_UPATE_PASSWORD);
        }
        Config config = Config.fromJSON(jsonNode.toString());
        return Redisson.create(config);
    }
}
