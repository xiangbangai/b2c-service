package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.CardActivity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/6
 * Time: 22:34
 * Description:
 */
@FeignClient(value = "b2c-service-query", contextId = "queryActivityRemote", path = "/query/activity")
public interface QueryActivityRemote {

    /**
     * 查询活动信息以及规则
     * @param cardActivity
     * @return
     */
    @PostMapping("/cardActivityAndRuleList")
    ResInfo cardActivityAndRuleList(@RequestBody CardActivity cardActivity);
}
