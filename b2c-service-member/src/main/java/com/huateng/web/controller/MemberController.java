package com.huateng.web.controller;

import com.huateng.base.BaseController;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.AccountNoRegister;
import com.huateng.data.vo.params.MemberCancellation;
import com.huateng.data.vo.params.VisualCardNo;
import com.huateng.web.service.MemberOperateService;
import com.huateng.web.service.ValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@Api(tags = "会员信息相关操作服务")
@RestController
public class MemberController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private MemberOperateService memberOperateService;


    @ApiOperation(value = "会员账户注销", notes = "返回成功或失败")
    @ApiImplicitParam(name = "memberCancellation", value = "会员账户参数", required = true, dataType = "MemberCancellation")
    @PostMapping("/operate/cancellation")
    public ResInfo cancellation(@RequestBody MemberCancellation memberCancellation) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForObject(memberCancellation, SysConstant.E04000001);
        this.validateService.validateForString(memberCancellation.getCustId(), SysConstant.E04000002);

        this.memberOperateService.cancellation(memberCancellation);

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "会员注册-主账户", notes = "返回会员号")
    @ApiImplicitParam(name = "accountNoRegister", value = "主账户信息", required = true, dataType = "AccountNoRegister")
    @PostMapping("/operate/accountNoRegister")
    public ResInfo accountNoRegister(@RequestBody AccountNoRegister accountNoRegister) throws Exception {
        ResInfo resInfo = new ResInfo();

        VisualCardNo visualCardNo = this.memberOperateService.accountNoRegister(accountNoRegister);

        resInfo.setData(JacksonUtil.toJson(visualCardNo));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }
}
