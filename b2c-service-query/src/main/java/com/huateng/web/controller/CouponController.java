package com.huateng.web.controller;

import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db1.TblCardUser;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.CardUser;
import com.huateng.web.service.QueryCouponService;
import com.huateng.web.service.ValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@Api(tags = "电子券相关查询服务")
@RestController
public class CouponController extends BaseController {

    @Resource
    private ValidateService validateService;

    @Resource
    private QueryCouponService queryCouponService;

    @ApiOperation(value = "查询电子券", notes = "如果有电子券则分页返回电子券列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cardUser", value = "电子券参数", required = true, dataType = "CardUser"),
            @ApiImplicitParam(name = "pageNum", value = "查询当前页", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "查询页面大小", required = true, dataType = "Integer")
    })
    @PostMapping("/coupon/pageList")
    public ResInfo queryCouponPageList(@RequestBody CardUser cardUser, Integer pageNum, Integer pageSize) throws Exception {
        ResInfo resInfo = new ResInfo();

        /** 参数校验 **/
        this.validateService.validateForObject(pageNum, SysConstant.E90000010);
        this.validateService.validateForObject(pageSize, SysConstant.E90000011);

        PageInfo<TblCardUser> pageInfo = queryCouponService.queryCouponPageList(cardUser, pageNum, pageSize);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        resInfo.setData(JacksonUtil.toJson(pageInfo));
        return resInfo;
    }
}
