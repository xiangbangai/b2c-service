package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.ServiceCustLimit;
import com.huateng.data.vo.ResInfo;
import com.huateng.web.service.CustomerLimitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/22
 * Time: 16:05
 * Description: 客户限制相关
 */
@Slf4j
@Api(tags = "会员限制相关查询服务")
@RestController
public class CustomerLimitController extends BaseController {

    @Resource
    private CustomerLimitService customerLimitService;

    @ApiOperation(value = "查询客户限额", notes = "返回限额对象")
    @ApiImplicitParam(name = "serviceCustLimit", value = "客户限额", required = true, dataType = "ServiceCustLimit")
    @PostMapping("/limit/queryLimit")
    public ResInfo queryLimit(@RequestBody ServiceCustLimit serviceCustLimit) throws Exception {
        ResInfo resInfo = new ResInfo();
        ServiceCustLimit limitInfo = this.customerLimitService.getLimitInfo(serviceCustLimit);
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        if (limitInfo != null) {
            resInfo.setData(JacksonUtil.toJson(limitInfo));
        }
        return resInfo;
    }

    @ApiOperation(value = "查询银联白名单", notes = "返回Boolean")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardNo", value = "银联卡号", required = true, dataType = "String")
    })
    @PostMapping("/limit/unionPayWhiteList")
    public ResInfo unionPayWhiteList(String cardNo){
        ResInfo resInfo = new ResInfo();

        Integer count = this.customerLimitService.checkCustUnionPayWhiteList(cardNo);

        resInfo.setData(count.toString());
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }
}
