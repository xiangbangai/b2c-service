package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.AccountNoRegister;
import com.huateng.data.vo.params.MemberCancellation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "b2c-service-member", contextId = "memberOperateRemote", path = "/member/operate")
public interface MemberOperateRemote {

    /**
     * 会员注销
     * @param memberCancellation
     * @return
     */
    @PostMapping("/cancellation")
    ResInfo cancellation(@RequestBody MemberCancellation memberCancellation);

    /**
     * 会员注册-主账户
     * @param accountNoRegister
     * @return
     */
    @PostMapping("/accountNoRegister")
    ResInfo accountNoRegister(@RequestBody AccountNoRegister accountNoRegister);
}
