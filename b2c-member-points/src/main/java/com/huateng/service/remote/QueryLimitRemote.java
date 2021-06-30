package com.huateng.service.remote;

import com.huateng.data.model.db2.ServiceCustLimit;
import com.huateng.data.vo.ResInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/22
 * Time: 16:19
 * Description:
 */
@FeignClient(value = "b2c-service-query", contextId = "customerLimitRemote", path = "/query/limit")
public interface QueryLimitRemote {

    @PostMapping("/queryLimit")
    ResInfo queryLimit(@RequestBody ServiceCustLimit serviceCustLimit);

    /**
     * 银联卡白名单
     * @param cardNo
     * @return
     */
    @PostMapping("/unionPayWhiteList")
    ResInfo queryCustUnionPayWhiteList(@RequestParam String cardNo);
}
