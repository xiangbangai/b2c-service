package com.huateng.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseService;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.json.res.ResAddPointsInfo;
import com.huateng.data.vo.params.*;
import com.huateng.service.remote.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class MemberPointsService extends BaseService {

    @Resource
    private MemberPointsRemote memberPointsRemote;
    @Resource
    private QueryLimitRemote queryLimitRemote;
    @Resource
    private QueryMemberPointsRemote queryMemberPointsRemote;
    @Resource
    private MemberPointsRelateRemote memberPointsRelateRemote;
    @Resource
    private QueryCustRemote queryCustRemote;

    /**
     * 保存订单，原定单存在则返回原定单
     * @param serviceOrder
     * @return
     */
    public ServiceOrder saveOrder(ServiceOrder serviceOrder) throws Exception {
        ResInfo resInfo = this.memberPointsRelateRemote.saveOrder(serviceOrder);
        return JacksonUtil.toObject(getResJson(resInfo),ServiceOrder.class);
    }

    /**
     * 积分兑换
     * @param id
     * @param exchangeGoods
     * @param custId
     * @param date
     * @return
     * @throws Exception
     */
    public ExchangeResult exchange(String id, String custId, Date date, List<ExchangeGoods> exchangeGoods) throws Exception{
        ResInfo resInfo = this.memberPointsRemote.exchange(id, custId, date, exchangeGoods);
        return JacksonUtil.toObject(getResJson(resInfo),ExchangeResult.class);
    }

    /**
     * 推送微信消息
     * @param sendWeChat
     * @param type
     */
    public void sendWeChatMsg(SendWeChat sendWeChat, Integer type) {
        this.memberPointsRelateRemote.sendWeChatMsg(sendWeChat, type);
    }


    /**
     * 查询会员限额信息
     * @param custId 会员号
     * @param limitType 限制类型
     * @param limitKey 限制key
     * @return 限制对象
     * @throws Exception
     */
    public ServiceCustLimit queryLimitInfo(String custId, Short limitType, String limitKey) throws Exception {
        ServiceCustLimit serviceCustLimit = new ServiceCustLimit();
        serviceCustLimit.setCustId(custId);
        serviceCustLimit.setLimitType(limitType);
        serviceCustLimit.setLimitKey(limitKey);
        ResInfo resInfo = this.queryLimitRemote.queryLimit(serviceCustLimit);
        return JacksonUtil.toObject(getResJson(resInfo),ServiceCustLimit.class);
    }

    /**
     * 查询订单信息
     * @param channel 渠道编号
     * @param repairSerialNo 原流水
     */
    public ServiceOrder  getBeforeOrderInfo(String channel, String repairSerialNo) throws Exception {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setChannel(channel);
        orderInfo.setReqSerialNo(repairSerialNo);
        ResInfo resInfo = this.queryMemberPointsRemote.queryOrderInfo(orderInfo);
        return JacksonUtil.toObject(getResJson(resInfo),ServiceOrder.class);
    }

    /**
     * 组装返回信息
     * @param response 返回信息
     * @param serviceOrder 订单信息
     */
    public void callBackInfo(Response response, ServiceOrder serviceOrder) throws Exception {
        /**查询是否会员**/
        ResInfo custResInfo = this.queryCustRemote.queryCustInfo(serviceOrder.getAcctId());
        TblCustInf tblCustInf = JacksonUtil.toObject(getResJson(custResInfo),TblCustInf.class);
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }
        custResInfo = null;

        BigDecimal expireBonus = null;
        String expireDate = DateUtil.date2String(serviceOrder.getHostDate(), DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG;
        List<String> validates = new ArrayList<>();
        validates.add(expireDate);

        BonusPlanDetailParams bonusPlanDetailParams = new BonusPlanDetailParams();
        bonusPlanDetailParams.setCustId(tblCustInf.getCustId());
        bonusPlanDetailParams.setBpPlanType(SysConstant.BP_PLAN_TYPE_DEFAULT);
        bonusPlanDetailParams.setValidates(validates);
        ResInfo planDetailsResInfo = this.queryMemberPointsRemote.queryBonusPlanDetail(bonusPlanDetailParams);
        List<TblBonusPlanDetail> bonusPlanDetailList = JacksonUtil.toObject(getResJson(planDetailsResInfo),new TypeReference<List<TblBonusPlanDetail>>(){});
        if(CollUtil.isNotEmpty(bonusPlanDetailList)){
            TblBonusPlanDetail planDetail = bonusPlanDetailList.get(0);
            if(expireDate.equals(planDetail.getValidDate())){
                expireBonus = planDetail.getValidBonus();
            }
        }
        bonusPlanDetailParams = null;
        planDetailsResInfo = null;
        validates = null;
        bonusPlanDetailList = null;

        ResAddPointsInfo resAddPointsInfo = new ResAddPointsInfo();
        resAddPointsInfo.setCustId(tblCustInf.getCustId());//custId
        resAddPointsInfo.setTaxName(tblCustInf.getCustInvoice());//发票抬头
        resAddPointsInfo.setIsAcceptEInvoice(tblCustInf.getIsAcceptEinvoice());//是否接收自动开电子发票
        resAddPointsInfo.setValidBonus(serviceOrder.getValidAfter());//原单操作之后的有效积分
        resAddPointsInfo.setTxnBonus(serviceOrder.getNumber());//原单交易积分
        resAddPointsInfo.setWillExpireBonus(NumberUtil.null2Zero(expireBonus));//即将到期积分
        resAddPointsInfo.setWillExpireDate(expireDate);//即将过期的有效期

        response.setReqDateTime(DateUtil.date2String(serviceOrder.getHostDate(), DateUtil.DATE_FORMAT_FULL));
        response.setResSerialNo(String.valueOf(serviceOrder.getId()));
        response.setData(resAddPointsInfo);
    }

    /**
     * 计算积分
     * @param computeBonus
     * @return
     * @throws Exception
     */
    public List<BonusAccItf> computeBonus(ComputeBonus computeBonus) throws Exception{
        ResInfo resInfo = this.memberPointsRemote.computeBonus(computeBonus);
        return JacksonUtil.toObject(getResJson(resInfo),new TypeReference<List<BonusAccItf>>() {});
    }

    /**
     * 积分入账
     * @param bonusEnter
     * @throws Exception
     */
    public ResAddPointsInfo bonusPlanEnter(BonusEnter bonusEnter) throws Exception{
        ResInfo resInfo = this.memberPointsRemote.bonusPlanEnter(bonusEnter);
        return JacksonUtil.toObject(getResJson(resInfo),ResAddPointsInfo.class);
    }

    /**
     * 积分冲正
     * @param autoBonusReversal
     * @throws Exception
     */
    public void bonusReversal(AutoBonusReversal autoBonusReversal) throws Exception{
        ResInfo resInfo = this.memberPointsRemote.bonusReversal(autoBonusReversal);
        getResJson(resInfo);
    }

    /**
     * 积分冲正 兼容老交易
     * @param tblBonusReversal
     */
    public void tblBonusReversal(AutoTblBonusReversal tblBonusReversal) throws Exception{
        ResInfo resInfo = this.memberPointsRemote.tblBonusReversal(tblBonusReversal);
        getResJson(resInfo);
    }

    /**
     * 更新订单状态
     * @param serviceOrder
     * @throws Exception
     */
    public void updateServiceOrder(ServiceOrder serviceOrder) throws Exception {
        ResInfo resInfo = this.memberPointsRelateRemote.updateServiceOrder(serviceOrder);
        getResJson(resInfo);
    }

    /**
     * 查询老交易订单
     * @param targetSerialNo
     * @param acctId
     * @return
     * @throws Exception
     */
    public TblOrder queryTblOrderInfo(String targetSerialNo, String acctId) throws Exception{
        ResInfo resInfo = this.queryMemberPointsRemote.queryTblOrderInfo(targetSerialNo,acctId);
        return JacksonUtil.toObject(getResJson(resInfo),TblOrder.class);
    }

    /**
     * 查询订单信息
     * @param orderInfo
     * @return
     */
    public ServiceOrder queryOrderInfo(OrderInfo orderInfo) throws Exception{
        ResInfo resInfo = this.queryMemberPointsRemote.queryOrderInfo(orderInfo);
        return JacksonUtil.toObject(getResJson(resInfo),ServiceOrder.class);
    }

    /**
     * 查询流水信息
     * @param memberPointsTxnParams
     */
    public PageInfo<ServiceOrderTxnInfo> queryTxnInfo(MemberPointsTxnParams memberPointsTxnParams) throws Exception {
        ResInfo resInfo = this.queryMemberPointsRemote.queryTxnInfo(memberPointsTxnParams);
        return JacksonUtil.toObject(getResJson(resInfo), new TypeReference<PageInfo<ServiceOrderTxnInfo>>(){});
    }

    /**
     * 积分调整
     * @param adjustOrder
     * @throws Exception
     */
    public void adjust(AdjustOrder adjustOrder) throws Exception{
        ResInfo resInfo = this.memberPointsRemote.adjust(adjustOrder);
        getResJson(resInfo);
    }
}
