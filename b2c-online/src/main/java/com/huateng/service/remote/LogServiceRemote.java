package com.huateng.service.remote;

import com.huateng.data.vo.params.LogInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/8/3
 * Time: 14:48
 * Description:
 */
@FeignClient(value = "b2c-service-log", contextId = "logServiceRemote", path = "/log/service")
public interface LogServiceRemote {


    /**
     * 上送日志服务
     * @param logInfo
     * @return
     */
    @PostMapping("/sendRemoteService")
    void sendRemoteService(@RequestBody LogInfo logInfo);
}
