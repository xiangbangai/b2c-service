package com.huateng.web.controller;

import cn.hutool.core.util.ReUtil;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.params.AccountNoRegister;
import com.huateng.data.vo.params.MemberCancellation;
import com.huateng.data.vo.params.VisualCardNo;
import com.huateng.web.service.CustService;
import com.huateng.web.service.MemberOperateService;
import com.huateng.web.service.ValidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 会员信息操作
 */
@Slf4j
@RestController
public class MemberController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private CustService custService;
    @Resource
    private MemberOperateService memberOperateService;


    /**
     * 会员注销
     * @return
     * @throws Exception
     */
    @PostMapping("/memberOperate/cancellation")
    public Response cancellation() throws Exception {
        Response response = new Response();
        response.setResCode(SysConstant.SYS_SUCCESS);

        Request request;
        MemberCancellation memberCancellation;
        request = getObject(MemberCancellation.class);
        memberCancellation = (MemberCancellation) request.getData();

        this.validateService.validateForString(memberCancellation.getCustId(), SysConstant.E01000010); //custId不可为空

        TblCustInf tblCustInf = this.custService.queryCustInfo(memberCancellation.getCustId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }

        memberCancellation.setTxnDate(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL));//请求时间
        memberCancellation.setCustId(tblCustInf.getCustId());
        this.memberOperateService.cancellation(memberCancellation);

        return response;
    }


    /**
     * 会员注册-主账户
     * @return
     * @throws Exception
     */
    @PostMapping("/memberOperate/accountNoRegister")
    public Response accountNoRegister() throws Exception {
        Response response = new Response();
        response.setResCode(SysConstant.SYS_SUCCESS);

        Request request;
        AccountNoRegister accountNoRegister;
        request = getObject(AccountNoRegister.class);
        accountNoRegister = (AccountNoRegister) request.getData();

        this.validateService.validateForString(accountNoRegister.getAccountNo(), SysConstant.E01000061); //accountNo不可为空
        if(!ReUtil.isMatch("^[0-9]{8}$", accountNoRegister.getAccountNo())){
            throw new BusinessException(getErrorInfo(SysConstant.E01000062));//accountNo格式不正确 8位数字
        }

        this.validateService.validateForObject(accountNoRegister.getIsCustAgreement(),SysConstant.E01000063);//isCustAgreement不可为空
        if(!ReUtil.isMatch("^[0-1]{1}$", accountNoRegister.getIsCustAgreement().toString())){
            throw new BusinessException(getErrorInfo(SysConstant.E01000064));//isCustAgreement非法
        }

        this.validateService.validateForDateTime(accountNoRegister.getAgreementTime(), DateUtil.DATE_FORMAT_FULL, SysConstant.E01000065);//agreementTime格式错误

        VisualCardNo visualCardNo = this.memberOperateService.accountNoRegister(accountNoRegister);
        response.setData(visualCardNo);

        return response;
    }
}
