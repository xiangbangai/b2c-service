package com.huateng.web.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloControl;
import com.huateng.data.model.db2.ServiceDict;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.json.res.ResAuditPointsInfo;
import com.huateng.data.vo.params.*;
import com.huateng.web.service.CustService;
import com.huateng.web.service.MemberPointsService;
import com.huateng.web.service.ValidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 提供给稽核系统的接口
 */
@Slf4j
@RestController
public class AuditController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private CustService custService;
    @Resource
    private ApolloControl apolloControl;
    @Resource
    private MemberPointsService memberPointsService;

    /**
     * 积分试算
     * @return
     * @throws Exception
     */
    @PostMapping("/audit/calculatePoints")
    public Response calculatePoints() throws Exception{
        Response response = new Response();
        response.setResCode(SysConstant.SYS_SUCCESS);
        Request request;
        AuditPoints auditPoints;
        request = getObject(AuditPoints.class);
        auditPoints = (AuditPoints) request.getData();

        //主机日期记录
        LocalDateTime localDateTime = DateUtil.getCurrentLocalDateTime();

        /**数据校验**/
        this.validateService.validateForDate(auditPoints.getBusinessDate(), DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000013); //businessDate格式错误
        this.validateService.validateForString(auditPoints.getAcctId(), SysConstant.E01000003); //acctId不可为空
        this.validateService.validateForString(auditPoints.getPosId(), SysConstant.E01000039); //posId不可为空
        this.validateService.validateForString(auditPoints.getStationId(), SysConstant.E01000040); //stationId不可为空
        if (auditPoints.getGoods() == null || auditPoints.getGoods().isEmpty()) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000016)); //goods不可为空
        }
        if (auditPoints.getPayment() == null || auditPoints.getPayment().isEmpty()) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000017)); //payment不可为空
        }

        /**二维码中的会员号，或报文中的acctId**/
        String acctId = "";
        /**二维码**/
        if(auditPoints.getAcctId().length() == 39) {
            QRCode code = new QRCode();
            code.setCode(auditPoints.getAcctId());
            code.setIsCheckTime(false);
            code.setIsCheckRepeat(false);
            acctId = this.custService.checkQRCode(code);
        }else{
            /**不是二维码，直接查询**/
            acctId = auditPoints.getAcctId();
        }

        /**校验手机作为条件**/
        validateChannelForMobile(acctId, request.getChannel());

        /**查询是否会员**/
        TblCustInf tblCustInf = this.custService.queryCustInfo(acctId);
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }

        /**开关检查**/
        //多油品 b2c.control.many.oil
        this.validateService.validateForObject(this.apolloControl.getManyOil(), SysConstant.E01000018);//多油品开关错误
        //手工录入油品 b2c.control.manual.oil
        this.validateService.validateForObject(this.apolloControl.getManualWorkOil(), SysConstant.E01000019); //手工录入油品开关错误

        //指定折扣不允许积分
        Map<String, ServiceDict> dictMap = getServiceDict().get(SysConstant.DICT_KEY_1003000);
        /**商品校验**/
        int oilCount = 0; //油品记录条数
        Set<String> idSet = new HashSet<>(); //商品列表id
        for(Goods s : auditPoints.getGoods()) {
            this.validateService.validateForString(s.getId(), SysConstant.E01000024); //id不可为空
            this.validateService.validateForString(s.getGoodsId(), SysConstant.E01000023); //goodsId不可为空
            this.validateService.validateForObject(s.getTotalPrice(), SysConstant.E01000025); //totalPrice不可为空
            this.validateService.validateForObject(s.getUnitPrice(), SysConstant.E01000026); //unitPrice不可为空
            this.validateService.validateForObject(s.getMiddleType(), SysConstant.E01000027); //middleType不可为空
            this.validateService.validateForObject(s.getNumber(), SysConstant.E01000028); //number不可为空
            this.validateService.validateForString(s.getGoodsName(), SysConstant.E01000029); //goodsName不可为空
            this.validateService.validateForObject(s.getGoodsType(), SysConstant.E01000030); //goodsType不可为空
            if (dictMap != null && s.getDiscountType() != null && !"".equals(s.getDiscountType())) {
                if (dictMap.containsKey(s.getDiscountType())) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000020), new Object[]{dictMap.get(s.getDiscountType()).getDictValue()}); //{0}折扣不允许积分
                }
            }
            if ((this.apolloControl.getManualWorkOil() == 1) && (SysConstant.GOODS_TYPE_MANUAL.equals(s.getGoodsType()))) {
                throw new BusinessException(getErrorInfo(SysConstant.E01000031)); //手工录入油品不允许积分
            }
            if (SysConstant.GOODS_TYPE_OIL.equals(s.getGoodsType())) { //油品
                oilCount++;
            }
            idSet.add(s.getId());
        }
        if (this.apolloControl.getManyOil() == 1 && oilCount > 1) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000032)); //多油品不允许积分
        }
        if (auditPoints.getGoods().size() != idSet.size()) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000033)); //商品列表id重复
        }
        /**
         * 会员明确不同意协议不可积分
         * 会员不知道协议但绑定手机号码的可积分
         * getIsAcceptEinvoice = 1 同意协议
         * getAgreementTime = null 不知道协议存在
         * getOpenId = null 未注册官微
         * getCustMobile = null 未绑定手机号码
         */
        if (tblCustInf.getIsCustAgreement() != 1 && tblCustInf.getAgreementTime() != null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000021)); //会员未同意服务协议，不允许积分
        }
        if (tblCustInf.getOpenId() == null && tblCustInf.getCustMobile() == null && !SysConstant.CUST_TYPE_ENTERPRISE.equals(tblCustInf.getCustType())) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000022)); //积分实体卡不允许积分
        }

        /** 检查支付方式 **/
        for (PayInfo payment : auditPoints.getPayment()) {
            String payType = payment.getPayType();
            String payInfo = payment.getPayInfo();
            this.validateService.validateForString(payType, SysConstant.E01000042); //payType不可为空
            this.validateService.validateForString(payInfo, SysConstant.E01000043); //payInfo不可为空
        }

        ResAuditPointsInfo resAuditPointsInfo = new ResAuditPointsInfo();
        String id = getSnowId();
        try {
            /** 积分计算 **/
            ComputeBonus computeBonus = new ComputeBonus();
            computeBonus.setChannel(request.getChannel());
            computeBonus.setReqSerialNo(request.getReqSerialNo());
            computeBonus.setReqDateTime(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL)); //给稽核平台使用
            computeBonus.setBusinessDate(DateUtil.string2Date4LocalDate(auditPoints.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));//营业日期
            computeBonus.setAcctId(acctId);
            computeBonus.setCustInf(tblCustInf);
            computeBonus.setStationId(auditPoints.getStationId());
            computeBonus.setPosId(auditPoints.getPosId());
            computeBonus.setGoods(auditPoints.getGoods());
            computeBonus.setPayment(auditPoints.getPayment());
            List<BonusAccItf> bonusList = this.memberPointsService.computeBonus(computeBonus);

            BigDecimal totalTxnPoints = BigDecimal.ZERO;
            if(CollUtil.isNotEmpty(bonusList)){
                totalTxnPoints = bonusList.stream().map(BonusAccItf::getTxnBonus).reduce(BigDecimal.ZERO,BigDecimal::add);
            }

            //积分自动进位需求
            List<AuditBonus> list = new ArrayList<>();
            if(CollUtil.isNotEmpty(bonusList)){
                Map<String,BonusAccItf> optMap = new HashMap<>();

                for (BonusAccItf bonusAccItf : bonusList) {
                    String validDate = bonusAccItf.getValidDate();
                    BonusAccItf date = optMap.get(validDate);
                    if(date != null){
                        date.setTxnBonus(date.getTxnBonus().add(bonusAccItf.getTxnBonus()));
                        optMap.put(validDate,date);
                    }else{
                        optMap.put(validDate,bonusAccItf);
                    }
                }

                for (String validate : optMap.keySet()) {
                    AuditBonus auditBonus = new AuditBonus();

                    BonusAccItf bonusAccItf = optMap.get(validate);
                    BigDecimal roundPoints = NumberUtil.round(bonusAccItf.getTxnBonus(), 0, RoundingMode.CEILING);

                    auditBonus.setValidDate(bonusAccItf.getValidDate());
                    auditBonus.setTxnBonus(roundPoints);
                    list.add(auditBonus);
                }
            }
            log.info("{}积分试算，该笔交易总产生积分：{}",request.getReqSerialNo(), totalTxnPoints);

            resAuditPointsInfo.setCustId(tblCustInf.getCustId());
            resAuditPointsInfo.setTxnTotalBonus(totalTxnPoints);
            resAuditPointsInfo.setBonusInfo(list);

        } catch (Exception e){
            throw e;
        }

        response.setData(resAuditPointsInfo);
        response.setResCode(SysConstant.SYS_SUCCESS);
        response.setResSerialNo(id);
        response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
        return response;
    }
}
