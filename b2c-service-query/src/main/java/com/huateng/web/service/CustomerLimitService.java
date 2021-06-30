package com.huateng.web.service;

import com.huateng.base.BaseService;
import com.huateng.data.db2.mapper.ServiceCustLimitMapper;
import com.huateng.data.db2.mapper.TblSpecialRuleMapper;
import com.huateng.data.model.db2.ServiceCustLimit;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/22
 * Time: 16:06
 * Description:
 */
@Service
public class CustomerLimitService extends BaseService {

    @Resource
    private ServiceCustLimitMapper serviceCustLimitMapper;
    @Resource
    private TblSpecialRuleMapper tblSpecialRuleMapper;

    /**
     * 获取限额信息
     * @param serviceCustLimit
     * @return
     */
    public ServiceCustLimit getLimitInfo(ServiceCustLimit serviceCustLimit) {
        return this.serviceCustLimitMapper.getLimitInfo(serviceCustLimit);
    }

    /**
     * 查询银联卡白名单
     * @param cardNo
     * @return
     */
    public int checkCustUnionPayWhiteList(String cardNo) {
        return tblSpecialRuleMapper.checkCustUnionPayWhiteList(cardNo);
    }
}
