package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "b2c-service-query", contextId = "queryStationRemote", path = "/query/station")
public interface QueryStationRemote {

    /**
     * 查询油站信息
     * @param stationId
     * @return
     */
    @PostMapping("/info")
    ResInfo queryStationInfo(@RequestParam String stationId);
}
