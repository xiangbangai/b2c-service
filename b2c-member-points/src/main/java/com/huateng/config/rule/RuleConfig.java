package com.huateng.config.rule;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "rule")
@Data
@Component
public class RuleConfig {
    public String locale;
    public String file;
}
