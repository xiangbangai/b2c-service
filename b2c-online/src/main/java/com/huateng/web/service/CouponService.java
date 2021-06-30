package com.huateng.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseService;
import com.huateng.common.util.JacksonUtil;
import com.huateng.data.model.db1.TblCardUser;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.CardUser;
import com.huateng.service.remote.QueryCouponRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class CouponService extends BaseService {

    @Resource
    private QueryCouponRemote queryCouponRemote;

    /**
     * 分页查询电子券
     * @param cardUser
     * @param pageNum
     * @param integer
     * @return
     */
    public PageInfo<TblCardUser> queryCouponPageList(CardUser cardUser, Integer pageNum, Integer integer) throws Exception {
        ResInfo resInfo = this.queryCouponRemote.couponPageList(cardUser,pageNum,integer);
        return JacksonUtil.toObject(getResJson(resInfo),  new TypeReference<PageInfo<TblCardUser>>(){});
    }
}
