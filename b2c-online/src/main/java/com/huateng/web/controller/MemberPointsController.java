package com.huateng.web.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.config.apollo.ApolloControl;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.json.req.AdjustPoints;
import com.huateng.data.vo.json.req.BonusReversal;
import com.huateng.data.vo.json.req.Exchange;
import com.huateng.data.vo.json.res.ResAddPointsInfo;
import com.huateng.data.vo.json.res.ResExchange;
import com.huateng.data.vo.params.*;
import com.huateng.service.remote.QueryMemberPointsRemote;
import com.huateng.web.service.CustService;
import com.huateng.web.service.InvoiceService;
import com.huateng.web.service.MemberPointsService;
import com.huateng.web.service.ValidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * @User Sam
 * @Date 2019/12/10
 * @Time 16:21
 * @Param 
 * @return 
 * @Description 会员积分操作
 */
@Slf4j
@RestController
public class MemberPointsController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private CustService custService;
    @Resource
    private ApolloControl apolloControl;
    @Resource
    private MemberPointsService memberPointsService;
    @Resource
    private InvoiceService invoiceService;
    @Resource
    private QueryMemberPointsRemote queryMemberPointsRemote;


    /**
     * 产生积分
     * @return
     */
    @PostMapping("/memberPoints/addPoints")
    public Response addPoints() throws Exception {
        Response response = new Response();
        response.setResCode(SysConstant.SYS_SUCCESS);
        Request request;
        AddPoints addPoints;
        request = getObject(AddPoints.class);
        addPoints = (AddPoints) request.getData();

        //主机日期记录
        LocalDateTime localDateTime = DateUtil.getCurrentLocalDateTime();

        /**数据校验**/
        this.validateService.validateForObject(addPoints.getRequestType(), SysConstant.E01000015); //requestType格式错误
        switch (addPoints.getRequestType()) {
            case 1:
                /**补传如果成功优先返回**/
                this.validateService.validateForString(addPoints.getRepairSerialNo(), SysConstant.E01000038); //repairSerialNo不可为空
                //查询原订单信息
                ServiceOrder serviceOrder = this.memberPointsService.getBeforeOrderInfo(request.getChannel(), addPoints.getRepairSerialNo());
                if (serviceOrder != null) {
                    switch (serviceOrder.getStatus()) {
                        case 1 :
                        case 3 :
                        case 4 :
                            log.info("补传原单成功,返回之前的结果");
                            this.memberPointsService.callBackInfo(response, serviceOrder);
                            return response;
                        case 2 :
                            log.error("补传流水原交易失败: " + addPoints.getRepairSerialNo());
                            break;
                        case 0 :
                            throw new BusinessException(getErrorInfo(SysConstant.E01000041)); //订单状态异常，请及时报障
                    }
                } else {
                    log.warn("补传流水不存在，继续处理：" + addPoints.getRepairSerialNo());
                }
                break;
            case 0:
                if (addPoints.getRepairSerialNo() != null && !"".equals(addPoints.getRepairSerialNo())) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000034)); //repairSerialNo必须为空
                }
                break;
            default:
                throw new BusinessException(getErrorInfo(SysConstant.E01000015)); //requestType格式错误
        }
        this.validateService.validateForDate(addPoints.getBusinessDate(), DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000013); //businessDate格式错误
        this.validateService.validateForString(addPoints.getAcctId(), SysConstant.E01000003); //acctId不可为空
        this.validateService.validateForString(addPoints.getPosId(), SysConstant.E01000039); //posId不可为空
        this.validateService.validateForString(addPoints.getStationId(), SysConstant.E01000040); //stationId不可为空
        if (addPoints.getGoods() == null || addPoints.getGoods().isEmpty()) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000016)); //goods不可为空
        }
        if (addPoints.getPayment() == null || addPoints.getPayment().isEmpty()) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000017)); //payment不可为空
        }

        /**二维码中的会员号，或报文中的acctId**/
        String acctId = "";
        /**二维码**/
        if(addPoints.getAcctId().length() == 39) {
            QRCode code = new QRCode();
            code.setCode(addPoints.getAcctId());
            if(addPoints.getRequestType() == 1){
                code.setIsCheckTime(false);
                code.setIsCheckRepeat(false);
            }
            acctId = this.custService.checkQRCode(code);
        }else{
            /**不是二维码，直接查询**/
            acctId = addPoints.getAcctId();
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
        for(Goods s : addPoints.getGoods()) {
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
        if (addPoints.getGoods().size() != idSet.size()) {
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
        for (PayInfo payment : addPoints.getPayment()) {
            String payType = payment.getPayType();
            String payInfo = payment.getPayInfo();
            this.validateService.validateForString(payType, SysConstant.E01000042); //payType不可为空
            this.validateService.validateForString(payInfo, SysConstant.E01000043); //payInfo不可为空
        }

        ResAddPointsInfo resAddPointsInfo = new ResAddPointsInfo();
        String id = getSnowId();
        ServiceOrder order = null;
        BonusEnter bonusEnter = null;
        try {
            /**保存主订单**/
            BigDecimal sum = addPoints.getGoods().stream().map(Goods::getTotalPrice).reduce(BigDecimal.ZERO,BigDecimal::add);
            ServiceOrder serviceOrder = new ServiceOrder();
            serviceOrder.setId(id);
            serviceOrder.setChannel(request.getChannel());//渠道
            serviceOrder.setPosId(addPoints.getPosId());//posId
            serviceOrder.setStationId(addPoints.getStationId());//油站id
            serviceOrder.setReqSerialNo(request.getReqSerialNo());//请求流水
            serviceOrder.setRepairSerialNo(addPoints.getRepairSerialNo());//补传流水
            serviceOrder.setChannelDate(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL));//请求时间
            serviceOrder.setBusinessDate(DateUtil.string2Date4LocalDate(addPoints.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));//营业日期
            serviceOrder.setHostDate(DateUtil.LocalDateTimeToDate(localDateTime));//主机时间
            serviceOrder.setNumber(BigDecimal.ZERO);//交易积分
            serviceOrder.setReturnableNumber(BigDecimal.ZERO);//可退积分
            serviceOrder.setOrderPrice(sum);//订单总金额金额
            serviceOrder.setReturnablePrice(sum);//订单可退金额
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_PROCESS);//订单状态为【处理中】
            serviceOrder.setOrderType(SysConstant.ORDER_TYPE_PRODUCE);//订单类型为【积分产生】
            serviceOrder.setCustId(tblCustInf.getCustId());
            serviceOrder.setAcctId(acctId);
            serviceOrder.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
            serviceOrder.setShiftId(addPoints.getShiftId());
            serviceOrder.setListNo(addPoints.getListNo());
            order = this.memberPointsService.saveOrder(serviceOrder);
            /**
             * 重复提交只控制成功状态，失败的可以重复提交，补传机制
             */
            if(order != null && SysConstant.ORDER_STATUS_SUCCESS.equals(order.getStatus())){
                log.info("重复提交原单交易成功，返回之前成功的信息");
                this.memberPointsService.callBackInfo(response, order);
                return response;
            }else if(order != null && SysConstant.ORDER_STATUS_FAILURE.equals(order.getStatus())){
                serviceOrder.setId(order.getId());
            }

            /** 积分计算 **/
            ComputeBonus computeBonus = new ComputeBonus();
            computeBonus.setChannel(request.getChannel());
            computeBonus.setReqSerialNo(request.getReqSerialNo());
            //computeBonus.setReqDateTime(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL)); //测试用
            computeBonus.setReqDateTime(DateUtil.LocalDateTimeToDate(localDateTime));
            computeBonus.setBusinessDate(DateUtil.string2Date4LocalDate(addPoints.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));//营业日期
            computeBonus.setAcctId(acctId);
            computeBonus.setCustInf(tblCustInf);
            computeBonus.setStationId(addPoints.getStationId());
            computeBonus.setPosId(addPoints.getPosId());
            computeBonus.setGoods(addPoints.getGoods());
            computeBonus.setPayment(addPoints.getPayment());
            List<BonusAccItf> bonusList = this.memberPointsService.computeBonus(computeBonus);

            BigDecimal totalTxnPoints = null;
            if(CollUtil.isNotEmpty(bonusList)){
                totalTxnPoints = bonusList.stream().map(BonusAccItf::getTxnBonus).reduce(BigDecimal.ZERO,BigDecimal::add);
            }
            log.info("该笔交易总产生积分：{}",NumberUtil.null2Zero(totalTxnPoints));

            /** 积分入账 **/
            bonusEnter = new BonusEnter();
            bonusEnter.setOrder(serviceOrder);
            bonusEnter.setCustInf(tblCustInf);
            bonusEnter.setBonus(bonusList);
            bonusEnter.setGoods(addPoints.getGoods());
            resAddPointsInfo = this.memberPointsService.bonusPlanEnter(bonusEnter);
        } catch (Exception e){
            if (order == null) {
                order = new ServiceOrder();
                order.setId(id);
                order.setStatus(SysConstant.ORDER_STATUS_FAILURE);
                this.memberPointsService.updateServiceOrder(order);
                order = null;
            }

            //查询原订单商品信息
            ResInfo orderDetailResInfo = this.queryMemberPointsRemote.queryNewOrderDetailByOrderId(bonusEnter.getOrder().getId());
            List<ServiceOrderDetail> orderDetailList = JacksonUtil.toObject(getResJson(orderDetailResInfo), new TypeReference<List<ServiceOrderDetail>>(){});
            if(orderDetailList == null || orderDetailList.isEmpty()){
                //保存因超限额不能积分的商品信息
                List<Goods> orderGoods = bonusEnter.getGoods();//客户提交商品
                ServiceOrderDetail serviceOrderDetail;
                for (Goods goods : orderGoods) {
                    serviceOrderDetail = new ServiceOrderDetail();
                    serviceOrderDetail.setId(goods.getId());//子订单id
                    serviceOrderDetail.setOrderId(bonusEnter.getOrder().getId());//订单id
                    serviceOrderDetail.setGoodsId(goods.getGoodsId());//商品id
                    serviceOrderDetail.setTotalPrice(goods.getTotalPrice());//商品总价
                    serviceOrderDetail.setUnitPrice(goods.getUnitPrice());//商品单价
                    serviceOrderDetail.setMiddleType(String.valueOf(goods.getMiddleType()));//商品中类
                    serviceOrderDetail.setLitType(String.valueOf(goods.getLitType()));//商品小类
                    serviceOrderDetail.setNumber(goods.getNumber());//商品数量
                    serviceOrderDetail.setReturnableNumber(goods.getNumber());//可退数量
                    serviceOrderDetail.setGoodsName(goods.getGoodsName());//商品名称
                    serviceOrderDetail.setGoodsType(goods.getGoodsType());//商品类型
                    serviceOrderDetail.setDiscountType(goods.getDiscountType());//优惠类型
                    String reqSerialNo = bonusEnter.getOrder().getReqSerialNo();
                    String channel = bonusEnter.getOrder().getChannel();
                    this.invoiceService.insertGoods(serviceOrderDetail,channel,reqSerialNo);
                }
            }
            throw e;
        }



        response.setData(resAddPointsInfo);
        response.setResCode(SysConstant.SYS_SUCCESS);
        response.setResSerialNo(id);
        response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
        return response;
    }

    /**
     * 积分兑换
     * @return
     */
    @PostMapping("/memberPoints/exchange")
    public Response exchange() throws Exception{
        Response response = new Response();
        Request request;
        Exchange exchange;
        request = getObject(Exchange.class);
        exchange = (Exchange) request.getData();

        //主机日期记录
        LocalDateTime localDateTime = DateUtil.getCurrentLocalDateTime();
        Date hostDate = DateUtil.LocalDateTimeToDate(localDateTime);

        /** 参数校验 **/
        //businessDate格式错误
        this.validateService.validateForDate(exchange.getBusinessDate(), DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000013);
        //acctId不可为空
        this.validateService.validateForString(exchange.getAcctId(), SysConstant.E01000003);
        //pos编号不可为空
        this.validateService.validateForString(exchange.getPosId(), SysConstant.E01000039);
        //油站编号不可为空
        this.validateService.validateForString(exchange.getStationId(), SysConstant.E01000040);
        //goods不可为空
        if (exchange.getGoods() == null || exchange.getGoods().isEmpty()) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000016));
        }
        //校验商品属性
        for (ExchangeGoods exchangeGoods : exchange.getGoods()) {
            this.validateService.validateForString(exchangeGoods.getGoodsId(), SysConstant.E01000023);
            this.validateService.validateForString(exchangeGoods.getId(), SysConstant.E01000024);
            this.validateService.validateForString(exchangeGoods.getGoodsName(), SysConstant.E01000029);
            this.validateService.validateForObject(exchangeGoods.getNumber(), SysConstant.E01000028);
            this.validateService.validateForObject(exchangeGoods.getTotalPrice(), SysConstant.E01000025);
            this.validateService.validateForObject(exchangeGoods.getUnitPrice(), SysConstant.E01000026);
        }

        /**不允许手机号作为凭证**/
        validateForMobile(exchange.getAcctId(), SysConstant.E01000044);

        /**非会员**/
        TblCustInf tblCustInf = this.custService.queryCustInfo(exchange.getAcctId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }

        //订单主键
        String id = getSnowId();

        /**保存主订单**/
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setId(id);
        serviceOrder.setChannel(request.getChannel());//渠道
        serviceOrder.setPosId(exchange.getPosId());//posId
        serviceOrder.setStationId(exchange.getStationId());//油站id
        serviceOrder.setReqSerialNo(request.getReqSerialNo());//请求流水
        serviceOrder.setChannelDate(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL));//请求时间
        serviceOrder.setBusinessDate(DateUtil.string2Date4LocalDate(exchange.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));//营业日期
        serviceOrder.setHostDate(hostDate);//主机时间
        serviceOrder.setStatus(SysConstant.ORDER_STATUS_PROCESS);//订单状态为【处理中】
        serviceOrder.setOrderType(SysConstant.ORDER_TYPE_EXCHANGE);//订单类型为【积分兑换】
        serviceOrder.setOperate(SysConstant.BONUS_OPERATE_DECREASE); //兑换减少积分
        serviceOrder.setCustId(tblCustInf.getCustId());//custId
        serviceOrder.setAcctId(exchange.getAcctId());
        serviceOrder.setMallId(exchange.getMallId());
        serviceOrder.setShiftId(exchange.getShiftId());
        serviceOrder.setListNo(exchange.getListNo());
        ServiceOrder result = this.memberPointsService.saveOrder(serviceOrder);
        serviceOrder = null;
        ResExchange resExchange = new ResExchange();

        /**重复提交**/
        if (result != null) {
            //原定单失败
            if(Arrays.binarySearch(SysConstant.ORDER_STATUS_SUCCESS_INCLUDE, result.getStatus()) < 0) {
                throw new BusinessException(getErrorInfo(SysConstant.E01000054)); //交易失败
            } else {
                TblBonusPlan tblBonusPlan = this.custService.pointsDetailList(tblCustInf.getCustId(), SysConstant.BP_PLAN_TYPE_DEFAULT);
                resExchange.setTotalBonus(tblBonusPlan.getValidBonus());
                resExchange.setExpBonus(NumberUtil.null2Zero(tblBonusPlan.getValidBonus2()));
                resExchange.setExpDate(DateUtil.getCurrentLocalDateTime(DateUtil.DATE_YEAR) + SysConstant.YEAR_FLAG);
                response.setData(resExchange);
                response.setResCode(SysConstant.SYS_SUCCESS);
                response.setResDateTime(DateUtil.date2String(result.getHostDate(), DateUtil.DATE_FORMAT_FULL));
                response.setResSerialNo(result.getId());
                return response;
            }
        }

        ExchangeResult exchangeResult;

        try {
            /** 调用积分服务处理 **/
            exchangeResult = this.memberPointsService.exchange(id, tblCustInf.getCustId(), hostDate, exchange.getGoods());
            BeanUtils.copyProperties(exchangeResult, resExchange);
            /**异步通知**/
            CompletableFuture.runAsync(() -> {
                SendWeChat sendWeChat = new SendWeChat();
                sendWeChat.setTitle(SysConstant.MEMBER_POINTS_MSG_TITLE);
                sendWeChat.setOpenId(tblCustInf.getOpenId());
                sendWeChat.setCardNo(tblCustInf.getCustId());
                sendWeChat.setCode(exchange.getStationId());
                sendWeChat.setIntegral(exchangeResult.getTotalConsume());
                sendWeChat.setDate(hostDate);
                sendWeChat.setTotalPoints(exchangeResult.getTotalBonus());
                this.memberPointsService.sendWeChatMsg(sendWeChat, SysConstant.SEND_WECHAT_MSG_EXCHANGE);
            });

        } catch (Exception e) {
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(id);
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_FAILURE);
            this.memberPointsService.updateServiceOrder(serviceOrder);
            serviceOrder = null;
            throw e;
        }




        response.setData(resExchange);
        response.setResCode(SysConstant.SYS_SUCCESS);
        response.setResSerialNo(id);
        response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
        return response;
    }


    /**
     * 积分兑换退货
     * @return
     * @throws Exception
     */
    // TODO: 2020/5/30 后续积分产生、积分兑换的冲正要分开，或者增加请求字段区分不同交易冲正，POS端同一笔交易流水一致，包括积分待发也会流水一致，导致冲正有问题
    @PostMapping("/memberPoints/bonusReversal")
    public Response bonusReversal() throws Exception{
        Response response = new Response();
        Request request;
        BonusReversal bonusReversal;
        request = getObject(BonusReversal.class);
        bonusReversal = (BonusReversal) request.getData();


        //主机日期记录
        LocalDateTime localDateTime = DateUtil.getCurrentLocalDateTime();

        /** 参数校验 **/
        //冲正类型
        switch (bonusReversal.getReverseType()) {
            case 1 : //部分冲正
                if(CollUtil.isEmpty(bonusReversal.getDetail())){
                    throw new BusinessException(getErrorInfo(SysConstant.E01000046));//detail不可为空
                }
                for (ReversalDetail reversalDetail : bonusReversal.getDetail()) {
                    this.validateService.validateForString(reversalDetail.getId(), SysConstant.E01000024);
                    this.validateService.validateForObject(reversalDetail.getNumber(), SysConstant.E01000028);
                    this.validateService.validateForObject(reversalDetail.getTotalPrice(), SysConstant.E01000025);
                }
                //bonusNumber不可为空
                this.validateService.validateForObject(bonusReversal.getBonusNumber(), SysConstant.E01000048);
                break;
            case 0 : //整单冲正
                break;
            default  :
                throw new BusinessException(getErrorInfo(SysConstant.E01000045));//冲正类型错误
        }
        //pos编号不可为空
        this.validateService.validateForString(bonusReversal.getPosId(), SysConstant.E01000039);
        //油站编号不可为空
        this.validateService.validateForString(bonusReversal.getStationId(), SysConstant.E01000040);
        //targetSerialNo不可为空
        this.validateService.validateForString(bonusReversal.getTargetSerialNo(),SysConstant.E01000047);
        //businessDate格式错误
        this.validateService.validateForDate(bonusReversal.getBusinessDate(), DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000013);
        //acctId不可为空
        this.validateService.validateForString(bonusReversal.getAcctId(), SysConstant.E01000003);


        /**校验手机作为参数**/
        validateChannelForMobile(bonusReversal.getAcctId(), request.getChannel());

        /**非会员**/
        TblCustInf tblCustInf = this.custService.queryCustInfo(bonusReversal.getAcctId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }

        //订单主键
        String id = getSnowId();

        /**保存主订单**/
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setId(id);
        serviceOrder.setChannel(request.getChannel());//渠道
        serviceOrder.setPosId(bonusReversal.getPosId());//posId
        serviceOrder.setStationId(bonusReversal.getStationId());//油站id
        serviceOrder.setReqSerialNo(request.getReqSerialNo());//请求流水
        serviceOrder.setTargetSerialNo(bonusReversal.getTargetSerialNo());//目标流水
        serviceOrder.setChannelDate(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL));//请求时间
        serviceOrder.setBusinessDate(DateUtil.string2Date4LocalDate(bonusReversal.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));//营业日期
        serviceOrder.setHostDate(DateUtil.LocalDateTimeToDate(localDateTime));//主机时间
        serviceOrder.setStatus(SysConstant.ORDER_STATUS_PROCESS);//订单状态为【处理中】
        serviceOrder.setOrderType(SysConstant.ORDER_TYPE_RETURN);//订单类型为【积分冲正】
        serviceOrder.setShiftId(bonusReversal.getShiftId());
        serviceOrder.setListNo(bonusReversal.getListNo());
        serviceOrder.setCustId(tblCustInf.getCustId());//custId
        serviceOrder.setAcctId(bonusReversal.getAcctId());

        Map<String,Integer> hasOrder = new HashMap<>();
        try {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setChannel(request.getChannel());
            orderInfo.setReqSerialNo(bonusReversal.getTargetSerialNo());
            orderInfo.setOrderType(SysConstant.ORDER_TYPE_EXCHANGE); //积分兑换
            orderInfo.setCustId(tblCustInf.getCustId());

            ServiceOrder oldOrder = this.memberPointsService.queryOrderInfo(orderInfo);//查询原单
            TblOrder tblOrder = null;
            if(oldOrder != null){
                /**原定单状态不正确**/
                if (Arrays.binarySearch(SysConstant.CAN_REVERSAL_STATUS, oldOrder.getStatus()) < 0) {
                    throw new BusinessException(getErrorInfo(SysConstant.E01000049));//原订单状态错误
                }

                hasOrder.put("hasOrder",1);
                /**支持整单重复冲**/
                if (oldOrder.getReturnableNumber().compareTo(BigDecimal.ZERO) == 0) {
                    response.setData(hasOrder);
                    response.setResCode(SysConstant.SYS_SUCCESS);
                    response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
                    response.setResSerialNo(id);
                    return response;
                }

                /**重复提交判断**/
                serviceOrder.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
                ServiceOrder order = this.memberPointsService.saveOrder(serviceOrder);
                if(order != null){
                    if(Arrays.binarySearch(SysConstant.ORDER_STATUS_SUCCESS_INCLUDE, order.getStatus()) < 0) {
                        throw new BusinessException(getErrorInfo(SysConstant.E01000054)); //交易失败
                    }
                    response.setData(hasOrder);
                    response.setResCode(SysConstant.SYS_SUCCESS);
                    response.setResDateTime(DateUtil.date2String(order.getHostDate(), DateUtil.DATE_FORMAT_FULL));
                    response.setResSerialNo(order.getId());
                    return response;
                }

                log.info("进行新交易冲正");
                AutoBonusReversal reversal = new AutoBonusReversal();
                reversal.setOrderId(id);
                reversal.setTargetOrderId(oldOrder.getId());
                reversal.setReverseType(bonusReversal.getReverseType());
                reversal.setCustId(tblCustInf.getCustId());
                reversal.setReverseNumber(bonusReversal.getBonusNumber());
                reversal.setHostDateTime(DateUtil.LocalDateTimeToDate(localDateTime));
                reversal.setDetails(bonusReversal.getDetail());
                this.memberPointsService.bonusReversal(reversal);
            }else{
                /**查询原单**/
                tblOrder  = this.memberPointsService.queryTblOrderInfo(bonusReversal.getTargetSerialNo(),bonusReversal.getAcctId());
                if(tblOrder != null){

                    if(!ArrayUtil.contains(SysConstant.OLD_CAN_REVERSAL_TYPE,tblOrder.getTxnCode())){
                        log.info("原单类型不支持冲正，类型：{}",tblOrder.getTxnCode());
                        hasOrder.put("hasOrder",0);
                        response.setData(hasOrder);
                        response.setResCode(SysConstant.SYS_SUCCESS);
                        response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
                        response.setResSerialNo(String.valueOf(id));
                        return response;
                    }

                    hasOrder.put("hasOrder",1);

                    log.info("进行老交易冲正");
                    Short operate = "c".equals(tblOrder.getBonusCDFlag()) ? SysConstant.BONUS_OPERATE_INCREASE : SysConstant.BONUS_OPERATE_DECREASE;
                    serviceOrder.setOperate(operate);
                    ServiceOrder order = this.memberPointsService.saveOrder(serviceOrder);
                    /**重复提交判断**/
                    if(order != null){
                        if(Arrays.binarySearch(SysConstant.ORDER_STATUS_SUCCESS_INCLUDE, order.getStatus()) < 0) {
                            throw new BusinessException(getErrorInfo(SysConstant.E01000054)); //交易失败
                        }
                        response.setData(hasOrder);
                        response.setResCode(SysConstant.SYS_SUCCESS);
                        response.setResDateTime(DateUtil.date2String(order.getHostDate(), DateUtil.DATE_FORMAT_FULL));
                        response.setResSerialNo(order.getId());
                        return response;
                    }

                    AutoTblBonusReversal tblBonusReversal = new AutoTblBonusReversal();
                    tblBonusReversal.setOrderId(id);
                    tblBonusReversal.setChannel(StrUtil.sub(request.getChannel(),0,6));//老交易渠道编号只有6位
                    tblBonusReversal.setStationId(bonusReversal.getStationId());
                    tblBonusReversal.setPosId(bonusReversal.getPosId());
                    tblBonusReversal.setReverseType(bonusReversal.getReverseType());
                    tblBonusReversal.setCustId(tblCustInf.getCustId());
                    tblBonusReversal.setReverseNumber(bonusReversal.getBonusNumber());
                    tblBonusReversal.setTargetOrderId(tblOrder.getOrderId());
                    tblBonusReversal.setHostDateTime(DateUtil.LocalDateTimeToDate(localDateTime));
                    tblBonusReversal.setBusinessDate(DateUtil.date2String(serviceOrder.getBusinessDate(),DateUtil.DATE_FORMAT_COMPACT));
                    tblBonusReversal.setDetails(bonusReversal.getDetail());
                    this.memberPointsService.tblBonusReversal(tblBonusReversal);
                }
            }

            if(oldOrder == null && tblOrder == null){
                hasOrder.put("hasOrder",0);
                log.info("新/老交易都查询不到原订单");
            }
        } catch (Exception e) {
            ServiceOrder order = new ServiceOrder();
            order.setId(id);
            order.setStatus(SysConstant.ORDER_STATUS_FAILURE);
            this.memberPointsService.updateServiceOrder(order);
            order = null;
            throw e;
        }

        response.setData(hasOrder);
        response.setResCode(SysConstant.SYS_SUCCESS);
        response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
        response.setResSerialNo(String.valueOf(id));
        return response;
    }


    /**
     * 积分赠送接口（用于官微积分抽奖赠送积分）
     * @return
     * @throws Exception
     */
    @PostMapping("/memberPoints/sendPoints")
    public Response adjustPoints()throws Exception{
        Response response = new Response();
        Request request;
        AdjustPoints adjustPoints;
        request = getObject(AdjustPoints.class);
        adjustPoints = (AdjustPoints) request.getData();

        //主机日期记录
        LocalDateTime localDateTime = DateUtil.getCurrentLocalDateTime();
        Date hostDate = DateUtil.LocalDateTimeToDate(localDateTime);

        /** 参数校验 **/
        //businessDate格式错误
        this.validateService.validateForDate(adjustPoints.getBusinessDate(), DateUtil.DATE_FORMAT_SHORT, SysConstant.E01000013);
        //custId不可为空
        this.validateService.validateForString(adjustPoints.getCustId(), SysConstant.E01000010);
        //pos编号不可为空
        this.validateService.validateForString(adjustPoints.getPosId(), SysConstant.E01000039);
        //油站编号不可为空
        this.validateService.validateForString(adjustPoints.getStationId(), SysConstant.E01000040);
        //number不可为空
        this.validateService.validateForObject(adjustPoints.getNumber(), SysConstant.E01000028);
        //pushMsg不可为空
        this.validateService.validateForString(adjustPoints.getPushMsg(), SysConstant.E01000058);
        //adjustName不可为空
        this.validateService.validateForString(adjustPoints.getAdjustName(), SysConstant.E01000059);
        //property不可为空
        this.validateService.validateForString(adjustPoints.getProperty(), SysConstant.E01000060);

        /**非会员**/
        TblCustInf tblCustInf = this.custService.queryCustInfo(adjustPoints.getCustId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }

        //订单主键
        String id = getSnowId();

        /**保存主订单**/
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setId(id);
        serviceOrder.setChannel(request.getChannel());//渠道
        serviceOrder.setPosId(adjustPoints.getPosId());//posId
        serviceOrder.setStationId(adjustPoints.getStationId());//油站id
        serviceOrder.setReqSerialNo(request.getReqSerialNo());//请求流水
        serviceOrder.setChannelDate(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL));//请求时间
        serviceOrder.setBusinessDate(DateUtil.string2Date4LocalDate(adjustPoints.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));//营业日期
        serviceOrder.setHostDate(hostDate);//主机时间
        serviceOrder.setNumber(adjustPoints.getNumber());
        serviceOrder.setReturnableNumber(adjustPoints.getNumber());
        serviceOrder.setStatus(SysConstant.ORDER_STATUS_PROCESS);//订单状态为【处理中】
        serviceOrder.setOrderType(SysConstant.ORDER_TYPE_REISSUE);//订单类型为【积分调整】
        serviceOrder.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
        serviceOrder.setCustId(tblCustInf.getCustId());
        serviceOrder.setAcctId(adjustPoints.getCustId());
        serviceOrder.setMallId(adjustPoints.getMallId());
        serviceOrder.setShiftId(adjustPoints.getShiftId());
        serviceOrder.setListNo(adjustPoints.getListNo());
        ServiceOrder result = this.memberPointsService.saveOrder(serviceOrder);
        serviceOrder = null;

        /**重复提交**/
        if (result != null) {
            //原定单失败
            if(Arrays.binarySearch(SysConstant.ORDER_STATUS_SUCCESS_INCLUDE, result.getStatus()) < 0) {
                throw new BusinessException(getErrorInfo(SysConstant.E01000054)); //交易失败
            } else {
                response.setResCode(SysConstant.SYS_SUCCESS);
                response.setResDateTime(DateUtil.date2String(result.getHostDate(), DateUtil.DATE_FORMAT_FULL));
                response.setResSerialNo(result.getId());
                return response;
            }
        }

        try {
            /** 调用积分服务处理 **/
            AdjustOrder adjustOrder = new AdjustOrder();
            adjustOrder.setServiceOrderId(id);
            adjustOrder.setStationId(adjustPoints.getStationId());
            adjustOrder.setCustId(tblCustInf.getCustId());
            adjustOrder.setOpenId(tblCustInf.getOpenId());
            adjustOrder.setOperate(SysConstant.BONUS_OPERATE_INCREASE);
            adjustOrder.setNumber(adjustPoints.getNumber());
            adjustOrder.setAdjustProperty(adjustPoints.getProperty());
            adjustOrder.setMallId(adjustPoints.getMallId());
            adjustOrder.setTxnDesc(adjustPoints.getPushMsg());
            adjustOrder.setTxnItems(adjustPoints.getAdjustName());
            adjustOrder.setHostDate(DateUtil.string2Date4LocalDateTime(request.getReqDateTime(),DateUtil.DATE_FORMAT_FULL));
            adjustOrder.setBusinessDate(DateUtil.string2Date4LocalDate(adjustPoints.getBusinessDate(),DateUtil.DATE_FORMAT_SHORT));
            this.memberPointsService.adjust(adjustOrder);

        } catch (Exception e) {
            serviceOrder = new ServiceOrder();
            serviceOrder.setId(id);
            serviceOrder.setStatus(SysConstant.ORDER_STATUS_FAILURE);
            this.memberPointsService.updateServiceOrder(serviceOrder);
            serviceOrder = null;
            throw e;
        }

        response.setResCode(SysConstant.SYS_SUCCESS);
        response.setResSerialNo(id);
        response.setResDateTime(DateUtil.getCurrentLocalDateTime(localDateTime, DateUtil.DATE_FORMAT_FULL));
        return response;
    }
}
