package com.huateng.service.remote;

import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.LabelGroupInfo;
import com.huateng.data.vo.params.LabelParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "b2c-service-query", contextId = "queryCustRemote", path = "/query/cust")
public interface QueryCustRemote {

    /**
     * 查询客户信息
     * @param id
     * @return
     */
    @PostMapping("/info")
    ResInfo queryCustInfo(@RequestParam String id);

    /**
     * 查询会员标签
     * @param labelParam
     * @return
     */
    @PostMapping("/custLabel")
    ResInfo queryCustLabel(LabelParam labelParam);

    /**
     * 查询会员基础信息
     * @param custId
     * @return
     */
    @PostMapping("/custUser")
    ResInfo queryCustUser(@RequestParam String custId);

    /**
     * 查询会员标签组
     * @param labelGroupInfo
     * @return
     */
    @PostMapping("/custLabelGroup")
    ResInfo custLabelGroup(LabelGroupInfo labelGroupInfo);
}
