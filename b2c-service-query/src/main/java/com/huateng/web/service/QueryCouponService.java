package com.huateng.web.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseService;
import com.huateng.data.db1.mapper.TblCardUserMapper;
import com.huateng.data.model.db1.TblCardUser;
import com.huateng.data.vo.params.CardUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class QueryCouponService extends BaseService {

    @Resource
    private TblCardUserMapper tblCardUserMapper;
    /**
     * 分页查询电子券
     * @param cardUser
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<TblCardUser> queryCouponPageList(CardUser cardUser, Integer pageNum, Integer pageSize) {
        return PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> tblCardUserMapper.queryCouponPageList(cardUser));
    }
}
