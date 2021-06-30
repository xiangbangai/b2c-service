package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.common.util.JacksonUtil;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.AccountNoRegister;
import com.huateng.data.vo.params.MemberCancellation;
import com.huateng.data.vo.params.VisualCardNo;
import com.huateng.service.remote.MemberOperateRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class MemberOperateService extends BaseService {

    @Resource
    private MemberOperateRemote memberOperateRemote;

    /**
     * 会员注销
     * @param memberCancellation
     */
    public void cancellation(MemberCancellation memberCancellation) throws Exception{
        ResInfo resInfo = this.memberOperateRemote.cancellation(memberCancellation);
        getResJson(resInfo);
    }

    /**
     * 会员注册-主账户
     * @param accountNoRegister
     * @return
     * @throws Exception
     */
    public VisualCardNo accountNoRegister(AccountNoRegister accountNoRegister) throws Exception{
        ResInfo resInfo = this.memberOperateRemote.accountNoRegister(accountNoRegister);
        return JacksonUtil.toObject(getResJson(resInfo), VisualCardNo.class);
    }
}
