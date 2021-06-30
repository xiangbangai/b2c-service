package com.huateng.web.controller;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.JacksonUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db2.*;
import com.huateng.data.vo.ResInfo;
import com.huateng.data.vo.params.BonusPlanDetailParams;
import com.huateng.data.vo.params.MemberPointsTxnParams;
import com.huateng.data.vo.params.OrderInfo;
import com.huateng.web.service.MemberPointsService;
import com.huateng.web.service.ValidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/12/12
 * Time: 10:25
 * Description:
 */
@Slf4j
@Api(tags = "积分相关查询")
@RestController
public class MemberPointsController extends BaseController {

    @Resource
    private ValidateService validateService;
    @Resource
    private MemberPointsService memberPointsService;


    /********* 积分总账相关 *********/
    @ApiOperation(value = "查询会员本年度积分计划(脏读)", notes = "返回会员本年度积分计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "custId", value = "会员号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "integralType", value = "积分计划，默认1111", required = true, dataType = "String")
    })
    @PostMapping("/memberPoints/pointsDetailList")
    public ResInfo pointsDetailList(String custId, String integralType) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(custId, SysConstant.E90000007);
        this.validateService.validateForString(integralType, SysConstant.E90000008);
        TblBonusPlan tblBonusPlan;
        try {
            tblBonusPlan = this.memberPointsService.queryPointsDetail(custId, integralType);
        } catch (MyBatisSystemException e) {
            if(e.contains(TooManyResultsException.class)){
                log.error("查询会员本年度积分计划，返回记录不止一条[{}][{}]",custId,integralType);
                throw new BusinessException(getErrorInfo(SysConstant.E90000004));
            }else{
                throw e;
            }
        }

        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        resInfo.setData(JacksonUtil.toJson(tblBonusPlan));
        return resInfo;
    }

    @ApiOperation(value = "查询会员积分总账户", notes = "返回会员积分总账户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "custId", value = "会员号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "bpPlanType", value = "积分类型，默认1111", required = true, dataType = "String")
    })
    @PostMapping("/memberPoints/bonusPlan")
    public ResInfo bonusPlan(String custId,String bpPlanType) throws Exception {
        ResInfo resInfo = new ResInfo();
        /**校验参数**/
        this.validateService.validateForString(custId, SysConstant.E90000007);
        this.validateService.validateForString(bpPlanType, SysConstant.E90000008);
        TblBonusPlan tblBonusPlan;
        try {
            tblBonusPlan = this.memberPointsService.queryBonusPlanByCustId(custId,bpPlanType);
        } catch (MyBatisSystemException e) {
            if(e.contains(TooManyResultsException.class)){
                log.error("查询会员积分总账户，返回记录不止一条[{}][{}]",custId,bpPlanType);
                throw new BusinessException(getErrorInfo(SysConstant.E90000004));
            }else{
                throw e;
            }
        }

        resInfo.setData(JacksonUtil.toJson(tblBonusPlan));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }



    /********* 积分明细相关 *********/
    @ApiOperation(value = "查询会员积分账户有效明细", notes = "返回会员积分账户有效明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "custId", value = "会员号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "bpPlanType", value = "积分类型，默认1111", required = true, dataType = "String")
    })
    @PostMapping("/memberPoints/bonusPlanDetail")
    public ResInfo bonusPlanDetail(@RequestBody BonusPlanDetailParams bonusPlanDetailParams) throws Exception {
        ResInfo resInfo = new ResInfo();
        /**校验参数**/
        this.validateService.validateForString(bonusPlanDetailParams.getCustId(), SysConstant.E90000007);
        this.validateService.validateForString(bonusPlanDetailParams.getBpPlanType(), SysConstant.E90000008);

        List<TblBonusPlanDetail> planDetail = this.memberPointsService.queryBonusPlanDetail(bonusPlanDetailParams);
        if(!planDetail.isEmpty()){
            resInfo.setData(JacksonUtil.toJson(planDetail));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询客户指定有效期积分明细", notes = "返回积分明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "custId", value = "会员号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "bpPlanType", value = "积分类型，默认1111", required = true, dataType = "String"),
            @ApiImplicitParam(name = "validates", value = "有效期", required = false, dataType = "List<String>")
    })
    @PostMapping("/memberPoints/queryBonusPlanDetails")
    public ResInfo queryBonusPlanDetails(String custId,String bpPlanType,@RequestBody List<String> validates)throws Exception{
        ResInfo resInfo = new ResInfo();

        List<TblBonusPlanDetail> list = this.memberPointsService.queryBonusPlanDetails(custId,bpPlanType,validates);

        if(CollUtil.isNotEmpty(list)){
            resInfo.setData(JacksonUtil.toJson(list));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }



    /********* 订单相关 *********/
    @ApiOperation(value = "查询订单信息", notes = "返回订单对象")
    @ApiImplicitParam(name = "orderInfo", value = "订单参数", required = true, dataType = "OrderInfo")
    @PostMapping("/memberPoints/queryOrderInfo")
    public ResInfo queryOrderInfo(@RequestBody OrderInfo orderInfo) throws Exception {
        ResInfo resInfo = new ResInfo();
        resInfo.setData(JacksonUtil.toJson(this.memberPointsService.getOrderInfo(orderInfo)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询订单信息", notes = "返回订单对象")
    @ApiImplicitParam(name = "id", value = "订单主键", required = true, dataType = "Long")
    @PostMapping("/memberPoints/queryOrderById")
    public ResInfo queryOrderById(String id) throws Exception {
        ResInfo resInfo = new ResInfo();
        resInfo.setData(JacksonUtil.toJson(this.memberPointsService.queryOrderById(id)));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询新订单商品", notes = "返回商品集合对象")
    @ApiImplicitParam(name = "orderId", value = "订单id", required = true, dataType = "Long")
    @PostMapping("/memberPoints/queryNewOrderDetailByOrderId")
    public ResInfo queryNewOrderDetailByOrderId(String orderId) throws Exception {
        ResInfo resInfo = new ResInfo();
        List<ServiceOrderDetail> orderDetails = this.memberPointsService.queryNewOrderDetailByOrderId(orderId);
        if(CollUtil.isNotEmpty(orderDetails)){
            resInfo.setData(JacksonUtil.toJson(orderDetails));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询新订单流水", notes = "返回流水集合对象")
    @ApiImplicitParam(name = "orderId", value = "订单id", required = true, dataType = "Long")
    @PostMapping("/memberPoints/queryNewBonusDetailByOrderId")
    public ResInfo queryNewBonusDetailByOrderId(String orderId) throws Exception {
        ResInfo resInfo = new ResInfo();
        List<ServiceBonusDetail> serviceBonusDetails = this.memberPointsService.queryNewBonusDetailByOrderId(orderId);
        if(CollUtil.isNotEmpty(serviceBonusDetails)){
            resInfo.setData(JacksonUtil.toJson(serviceBonusDetails));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询老交易订单", notes = "返回订单对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "acqSsn", value = "请求流水", required = true, dataType = "String"),
            @ApiImplicitParam(name = "acctId", value = "客户号", required = true, dataType = "String")
    })
    @PostMapping("/memberPoints/queryTblOrderInfo")
    public ResInfo queryTblOrderInfo(String acqSsn,String acctId)throws Exception{
        ResInfo resInfo = new ResInfo();

        TblOrder order = this.memberPointsService.queryTblOrderInfo(acqSsn,acctId);

        resInfo.setData(JacksonUtil.toJson(order));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询老交易订单", notes = "返回订单对象")
    @ApiImplicitParam(name = "orderId", value = "订单主键", required = true, dataType = "String")
    @PostMapping("/memberPoints/queryTblOrderByOrderId")
    public ResInfo queryTblOrderByOrderId(String orderId)throws Exception{
        ResInfo resInfo = new ResInfo();

        TblOrder order = this.memberPointsService.queryTblOrderByOrderId(orderId);

        resInfo.setData(JacksonUtil.toJson(order));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询交易流水", notes = "返回交易流水对象")
    @ApiImplicitParam(name = "acqSsn", value = "流水号", required = true, dataType = "String")
    @PostMapping("/memberPoints/queryTxnDetailByAcqSsn")
    public ResInfo queryTxnDetailByAcqSsn(String acqSsn)throws Exception{
        ResInfo resInfo = new ResInfo();

        TblTxnDetail txnDetail = this.memberPointsService.queryTxnDetailByAcqSsn(acqSsn);

        resInfo.setData(JacksonUtil.toJson(txnDetail));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询订单商品", notes = "返回商品集合")
    @ApiImplicitParam(name = "orderId", value = "订单编号", required = true, dataType = "String")
    @PostMapping("/memberPoints/queryOrderDetailByOrderId")
    public ResInfo queryOrderDetailByOrderId(String orderId)throws Exception{
        ResInfo resInfo = new ResInfo();

        List<TblOrderDetail> orderDetail = this.memberPointsService.queryOrderDetailByOrderId(orderId);

        if(CollUtil.isNotEmpty(orderDetail)){
            resInfo.setData(JacksonUtil.toJson(orderDetail));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询订单积分流水", notes = "返回积分流水集合")
    @ApiImplicitParam(name = "bonusSsn", value = "积分流水号", required = true, dataType = "String")
    @PostMapping("/memberPoints/queryBonusDetailByBonusSsn")
    public ResInfo queryBonusDetailByBonusSsn(String bonusSsn)throws Exception{
        ResInfo resInfo = new ResInfo();

        List<TblBonusDetail> bonusDetail = this.memberPointsService.queryBonusDetailByBonusSsn(bonusSsn);

        if(CollUtil.isNotEmpty(bonusDetail)){
            resInfo.setData(JacksonUtil.toJson(bonusDetail));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }



    /********* 其他 *********/
    @ApiOperation(value = "查询客户待发积分", notes = "返回结果集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "custId", value = "会员号", required = true, dataType = "String")
    })
    @PostMapping("/memberPoints/bonusDelay")
    public ResInfo queryCustBonusDelay(String custId) throws Exception{
        ResInfo resInfo = new ResInfo();

        TblBonusDelay  tblBonusDelay = this.memberPointsService.queryCustBonusDelay(custId);

        if(tblBonusDelay != null){
            resInfo.setData(JacksonUtil.toJson(tblBonusDelay));
        }
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

    @ApiOperation(value = "查询积分流水信息", notes = "返回积分流水对象")
    @ApiImplicitParam(name = "memberPointsTxnParams", value = "订单流水参数", required = true, dataType = "MemberPointsTxnParams")
    @PostMapping("/memberPoints/queryTxnInfoPageList")
    public ResInfo queryTxnInfoPageList(@RequestBody MemberPointsTxnParams memberPointsTxnParams) throws Exception {
        ResInfo resInfo = new ResInfo();

        /**校验参数**/
        this.validateService.validateForString(memberPointsTxnParams.getCustId(), SysConstant.E90000007);
        this.validateService.validateForObject(memberPointsTxnParams.getPageNum(), SysConstant.E90000010);
        this.validateService.validateForObject(memberPointsTxnParams.getPageSize(), SysConstant.E90000011);
        this.validateService.validateForObject(memberPointsTxnParams.getBeginDate(), SysConstant.E90000003);
        this.validateService.validateForObject(memberPointsTxnParams.getEndDate(), SysConstant.E90000009);
        // TODO: 2020/4/22 后续待系统稳定后需要删除查询旧表部分
        PageInfo<ServiceOrderTxnInfo> pageInfo = this.memberPointsService.queryTxnInfo(memberPointsTxnParams);

        resInfo.setData(JacksonUtil.toJson(pageInfo));
        resInfo.setResCode(SysConstant.SYS_SUCCESS);
        return resInfo;
    }

}
