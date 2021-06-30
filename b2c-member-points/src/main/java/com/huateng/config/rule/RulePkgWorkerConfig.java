package com.huateng.config.rule;

import com.huateng.toprules.adapter.RulePkgWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class RulePkgWorkerConfig {
    @Resource
    private RuleConfig ruleConfig;

    @Bean
    public RulePkgWorker rulePkgUtil() {
        return new RulePkgWorker(ruleConfig);
    }
}
