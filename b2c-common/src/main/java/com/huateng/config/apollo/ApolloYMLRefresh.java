package com.huateng.config.apollo;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.huateng.common.util.SysConstant;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2018-11-24
 * Time: 19:24
 * Description:
 */
@Component
public class ApolloYMLRefresh {

    @Resource
    private RefreshScope refreshScope;

    @ApolloConfigChangeListener({"app-yml.yml"})
    public void onChange(ConfigChangeEvent changeEvent) {
        boolean configChange = false;
        for (String changedKey : changeEvent.changedKeys()) {
            if (changedKey.startsWith(SysConstant.APOLLO_START_KEY_YML)) {//key头部
                configChange = true;
                break;
            }
        }
        if (!configChange) {
            return;
        }
        //bean使用简单名称
        refreshScope.refresh(SysConstant.APOLLO_BEAN_APOLLO_YML);

    }
}
