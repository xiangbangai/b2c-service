package com.huateng.web.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseService;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.db2.mapper.*;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.params.BonusPlanDetailParams;
import com.huateng.data.vo.params.MemberPointsTxnParams;
import com.huateng.data.vo.params.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/12/12
 * Time: 10:29
 * Description:
 */
@Slf4j
@Service
public class MemberPointsService extends BaseService {

    @Resource
    private TblBonusPlanMapper tblBonusPlanMapper;
    @Resource
    private TblBonusPlanDetailMapper tblBonusPlanDetailMapper;
    @Resource
    private TblBonusDelayMapper tblBonusDelayMapper;
    @Resource
    private ServiceOrderMapper serviceOrderMapper;
    @Resource
    private ServiceOrderDetailMapper serviceOrderDetailMapper;
    @Resource
    private ServiceBonusDetailMapper serviceBonusDetailMapper;
    @Resource
    private TblOrderMapper tblOrderMapper;
    @Resource
    private TblTxnDetailMapper tblTxnDetailMapper;
    @Resource
    private TblOrderDetailMapper tblOrderDetailMapper;
    @Resource
    private TblBonusDetailMapper tblBonusDetailMapper;

    /**
     * 查询积分（脏读）
     * @param custId
     * @param integralType
     */
    @Transactional(transactionManager = "dataSourceTransactionManagerDb2",isolation = Isolation.READ_UNCOMMITTED)
    public TblBonusPlan queryPointsDetail(String custId, String integralType) {
        String date = DateUtil.getCurrentDateyyyy() + SysConstant.YEAR_LAST_DAY;
        return this.tblBonusPlanMapper.queryCustIntegral(custId, integralType, date);
    }

    /**
     * 查询会员积分计划详情
     * @param bonusPlanDetailParams
     * @return
     */
    public List<TblBonusPlanDetail> queryBonusPlanDetail(BonusPlanDetailParams bonusPlanDetailParams) {
        return tblBonusPlanDetailMapper.queryBonusPlanDetail(bonusPlanDetailParams);
    }



    /**
     * 查询会员积分信息
     * @param custId
     * @param bpPlanType
     * @return
     */
    public TblBonusPlan queryBonusPlanByCustId(String custId,String bpPlanType) {
        return tblBonusPlanMapper.queryBonusPlanByCustId(custId,bpPlanType);
    }

    /**
     * 查询客户补发
     * @param custId
     * @return
     */
    public TblBonusDelay queryCustBonusDelay(String custId) {
        return this.tblBonusDelayMapper.queryCustBonusDelay(custId);
    }

    public ServiceOrder getOrderInfo(OrderInfo orderInfo) {
        return this.serviceOrderMapper.getOrderInfo(orderInfo);
    }


    /**
     * 查询订单
     * @param id
     * @return
     */
    public ServiceOrder queryOrderById(String id) {
        return this.serviceOrderMapper.queryOrderById(id);
    }

    /**
     * 查询订单商品
     * @param id
     * @return
     */
    public List<ServiceOrderDetail> queryNewOrderDetailByOrderId(String id) {
        return this.serviceOrderDetailMapper.queryOrderDetailByOrderId(id);
    }

    /**
     * 查询订单流水
     * @param id
     * @return
     */
    public List<ServiceBonusDetail> queryNewBonusDetailByOrderId(String id) {
        return this.serviceBonusDetailMapper.queryBonusDetailByOrderId(id);
    }

    /**
     * 查询老交易订单
     * @param acqSsn
     * @param acctId
     * @return
     */
    public TblOrder queryTblOrderInfo(String acqSsn, String acctId) {
        return this.tblOrderMapper.queryTblOrderInfo(acqSsn,acctId);
    }

    /**
     * 查询老交易订单
     * @param orderId
     * @return
     */
    public TblOrder queryTblOrderByOrderId(String orderId) {
        return this.tblOrderMapper.queryTblOrderByOrderId(orderId);
    }

    /**
     * 查询交易流水
     * @param acqSsn
     * @return
     */
    public TblTxnDetail queryTxnDetailByAcqSsn(String acqSsn) {
        return this.tblTxnDetailMapper.getByAcqSsn(acqSsn);
    }

    /**
     * 查询订单商品
     * @param orderId
     * @return
     */
    public List<TblOrderDetail> queryOrderDetailByOrderId(String orderId) {
        return this.tblOrderDetailMapper.getByOrderId(orderId);
    }

    /**
     * 查询订单积分流水
     * @param bonusSsn
     * @return
     */
    public List<TblBonusDetail> queryBonusDetailByBonusSsn(String bonusSsn) {
        return this.tblBonusDetailMapper.getByBonusSsn(bonusSsn);
    }

    /**
     * 查询客户积分有效期
     * @param custId
     * @param bpPlanType
     * @param validates
     * @return
     */
    public List<TblBonusPlanDetail> queryBonusPlanDetails(String custId, String bpPlanType, List<String> validates) {
        return this.tblBonusPlanDetailMapper.queryBonusPlanDetails(custId,bpPlanType,validates);
    }

    /**
     * 查询积分流水
     * @param memberPointsTxnParams
     * @return
     */
    public PageInfo<ServiceOrderTxnInfo> queryTxnInfo(MemberPointsTxnParams memberPointsTxnParams) {
        PageInfo<ServiceOrderTxnInfo> info = PageHelper.startPage(memberPointsTxnParams.getPageNum(), memberPointsTxnParams.getPageSize())
                .doSelectPageInfo(() -> this.serviceOrderMapper.queryTxnInfo(memberPointsTxnParams));
        if(!info.getList().isEmpty()) {
            List<String> list = info.getList().stream().map(ServiceOrderTxnInfo::getId).collect(Collectors.toList());
            List<ServiceOrderDetail> result = this.serviceOrderDetailMapper.queryOrderDetailByOrderList(memberPointsTxnParams.getBeginDate(), memberPointsTxnParams.getEndDate(),
                    memberPointsTxnParams.getCustId(), list.toArray());
            list = null;
            Map<String, List<ServiceOrderDetail>> map = new HashMap<>();
            List<ServiceOrderDetail> add = null;
            String tempId = "";
            for (int i = 0; i < result.size(); i++) {
                if (i == 0) {
                    add = new ArrayList<>();
                    add.add(result.get(i));
                    tempId = result.get(i).getOrderId();
                } else {
                    if (tempId.equals(result.get(i).getOrderId())) {
                        add.add(result.get(i));
                    } else {
                        map.put(tempId, add);
                        add = new ArrayList<>();
                        add.add(result.get(i));
                        tempId = result.get(i).getOrderId();
                    }
                    if (i == (result.size() - 1)) {
                        map.put(tempId, add);
                    }
                }
                if (result.size() == 1) {
                    map.put(tempId, add);
                }
            }
            add = null;
            result = null;
            info.getList().stream().forEach(v -> v.setOrderDetails(map.get(v.getId())));
        }

        return info;
    }
}
