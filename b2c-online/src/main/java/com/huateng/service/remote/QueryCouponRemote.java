package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.CardUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/6
 * Time: 22:32
 * Description:
 */
@FeignClient(value = "b2c-service-query", contextId = "queryCouponRemote", path = "/query/coupon")
public interface QueryCouponRemote {

    /**
     * 分页查询电子券
     * @param cardUser
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("/pageList")
    ResInfo couponPageList(@RequestBody CardUser cardUser, @RequestParam Integer pageNum, @RequestParam Integer pageSize);
}
