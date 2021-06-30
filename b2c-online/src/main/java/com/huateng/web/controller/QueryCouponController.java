package com.huateng.web.controller;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.huateng.base.BaseController;
import com.huateng.common.util.BusinessException;
import com.huateng.common.util.DateUtil;
import com.huateng.common.util.SysConstant;
import com.huateng.data.model.db1.CardActivityAndRule;
import com.huateng.data.model.db1.TblCardUser;
import com.huateng.data.model.db2.TblCustInf;
import com.huateng.data.vo.json.Request;
import com.huateng.data.vo.json.Response;
import com.huateng.data.vo.json.req.CouponUsable;
import com.huateng.data.vo.json.req.QueryCoupon;
import com.huateng.data.vo.json.res.CouponInfo;
import com.huateng.data.vo.json.res.ResCouponInfo;
import com.huateng.data.vo.json.res.ResCouponUsable;
import com.huateng.data.vo.params.CardActivity;
import com.huateng.data.vo.params.CardUser;
import com.huateng.web.service.CardActivityService;
import com.huateng.web.service.CouponService;
import com.huateng.web.service.CustService;
import com.huateng.web.service.ValidateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
public class QueryCouponController extends BaseController {

    @Resource
    private ValidateService validateService;

    @Resource
    private CouponService couponService;

    @Resource
    private CardActivityService cardActivityService;
    @Resource
    private CustService custService;


    /**
     * 查询电子券信息
     * @return
     * @throws Exception
     */
    @PostMapping("/query/coupon/cust/pageList")
    public Response queryCouponInfo() throws Exception{
        Response response = new Response();
        ResCouponInfo data = new ResCouponInfo();
        Request request;
        QueryCoupon queryCoupon;
        request = getObject(QueryCoupon.class);
        queryCoupon = (QueryCoupon) request.getData();

        /** 验证参数 **/
        String custId = queryCoupon.getCustId();
        Integer pageNum = queryCoupon.getPageNum();
        Integer pageSize = queryCoupon.getPageSize();
        this.validateService.validateForString(custId, SysConstant.E01000010);
        this.validateService.validateForObject(pageNum, SysConstant.E01000011);
        this.validateService.validateForObject(pageSize, SysConstant.E01000004);
        /**校验手机作为参数**/
        validateChannelForMobile(custId, request.getChannel());
        /**先查询会员**/
        TblCustInf tblCustInf = this.custService.queryCustInfo(custId);
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }
        this.validateService.validateForObject(pageNum, SysConstant.E01000011);
        queryCoupon = null;

        /** 查询3个月内的活动 **/
        CardActivity cardActivity = new CardActivity();
        //3个月有效期的活动
        cardActivity.setValidDate(DateUtil.getPlusMonths4LocalDate(-3,DateUtil.DATE_FORMAT_COMPACT));
        cardActivity.setCardStatus(SysConstant.CARD_ACTIVITY_IS_OK);
        List<CardActivityAndRule> activityList = this.cardActivityService.queryCardActivityAndRuleList(cardActivity);
        if(activityList == null){
            data.setPageNum(pageNum);
            data.setPageSize(pageSize);
            data.setPages(0);
            data.setTotal(0);
            response.setData(data);
            return response;
        }
        Map<String, CardActivityAndRule> cardActivityAndRuleMap = new HashMap<>();
        Object[] allvalidActivityCode = activityList.stream().map(s -> {
            cardActivityAndRuleMap.put(String.valueOf(s.getCardCode()), s);
            return s.getCardCode();
        }).toArray();
        cardActivity = null;
        activityList = null;

        /** 分页查询电子券 **/
        CardUser cardUser = new CardUser();
        cardUser.setCustId(tblCustInf.getCustId());
        cardUser.setValidCardCodes(allvalidActivityCode);
        PageInfo<TblCardUser> pageInfo =  this.couponService.queryCouponPageList(cardUser,pageNum,pageSize);
        cardUser = null;


        List<CouponInfo> list = null;
        List<TblCardUser> cardUserList;
        if(pageInfo.getSize() > 0){
            cardUserList = pageInfo.getList();
            list = buildResponseVo(cardUserList, cardActivityAndRuleMap);
        }

        /** 封装响应数据 **/
        data.setPageNum(pageInfo.getPageNum());
        data.setPageSize(pageInfo.getPageSize());
        data.setPages(pageInfo.getPages());
        data.setTotal(pageInfo.getTotal());

        data.setList(list);

        response.setResCode(SysConstant.SYS_SUCCESS);
        response.setData(data);
        return response;
    }


    /**
     * 封装数据
     * @param cardUserList
     * @param activityMap
     */
    private List<CouponInfo> buildResponseVo(List<TblCardUser> cardUserList, Map<String, CardActivityAndRule> activityMap){
        List<CouponInfo> list = new ArrayList<CouponInfo>();
        if(cardUserList == null || activityMap == null ||
                cardUserList.isEmpty() || activityMap.isEmpty()){
            return list;
        }
        cardUserList.stream().forEach(cardUser ->{
            CouponInfo vo = new CouponInfo();
            CardActivityAndRule activity = activityMap.get(String.valueOf(cardUser.getCardCode()));
            vo.setCardId(cardUser.getCouponId());//卡券唯一标识
            vo.setCardName(activity.getCardName());//电子券名称
            vo.setCardCode(activity.getCardCode());//活动编号
            vo.setCardGetDate(cardUser.getCreateGetDate());//电子券领取日期
            vo.setCardStatus(cardUser.getCardStatus());//电子券使用状态
            vo.setCardType(activity.getCardType());//卡券类型-0：抵扣券1：折扣券 2:商品券
            vo.setSetMoney(activity.getSetMoney());//优惠金额（如果是折扣卷，这为最大优惠金额）
            vo.setCardDiscount(activity.getCardDiscount());//折扣比例，折扣卷为必填
            vo.setUseImpose(activity.getUseImpose());//使用限制金额（0为没有限制）
            vo.setStationId(activity.getStationId());//油站编码

            if(StrUtil.isNotBlank(cardUser.getStartDate())  && StrUtil.isNotBlank(cardUser.getEndDate())){
                vo.setCardBeginDate(cardUser.getStartDate());//电子券有效开始日期
                vo.setCardEndDate(cardUser.getEndDate());//电子券有效结束日期
            }else{
                vo.setCardBeginDate(activity.getCardBegintime());//电子券有效开始日期
                vo.setCardEndDate(activity.getCardEndtime());//电子券有效结束日期
            }

            vo.setActivityTitel(activity.getActivityTitel());//电子券说明
            vo.setTxnMoneyOil(activity.getTxnMoneyOil());//使用电子券油品消费金额限制
            vo.setTxnNumberOil(activity.getTxnNumberOil());//使用电子券油品消费数量限制
            vo.setTxnMoneyNotOil(activity.getTxnMoneyNotOil());//使用电子券非油品消费金额限制
            vo.setTxnNumberNotOil(activity.getTxnNumberNotOil());//使用电子券非油品消费数量限制
            vo.setDayOfWeek(this.exchangeDayOfWeek(activity.getDayOfWeek()));
            vo.setDayOfMon(this.exchangeDayOfMon(activity.getDayOfMon()));
            list.add(vo);
        });

        return list;
    }

    /**
     * 处理每周描述
     * @param dayOfWeek
     * @return
     */
    private String exchangeDayOfWeek(String dayOfWeek) {
        StringBuilder weekDetail = new StringBuilder("");
        if(StrUtil.isNotBlank(dayOfWeek)) {
            weekDetail.append("逢");
            if(dayOfWeek.length() == 1) {
                String desc = DateUtil.dayOfWeek.getDescByValue(dayOfWeek);
                weekDetail.append(desc);
            } else {
                String[] split = dayOfWeek.split(",");
                for(int i = 0;i<split.length;i++) {
                    String descByValue = DateUtil.dayOfWeek.getDescByValue(split[i]);
                    if(i==split.length-1) {
                        weekDetail.append(descByValue);
                    } else {
                        weekDetail.append(descByValue).append("及");
                    }
                }
            }
            weekDetail.append("可用");
        }
        return weekDetail.toString();
    }

    /**
     * 处理每月描述
     * @param dayOfMon
     * @return
     */
    private String exchangeDayOfMon(String dayOfMon) {
        StringBuilder monDetail = new StringBuilder("");
        if(StrUtil.isNotBlank(dayOfMon)) {
            monDetail.append("逢");
            if(dayOfMon.length() == 1) {
                monDetail.append(dayOfMon).append("号");
            } else {
                String[] split = dayOfMon.split(",");
                for(int i = 0;i<split.length;i++) {
                    if(i==split.length-1) {
                        monDetail.append(split[i]).append("号");
                    } else {
                        monDetail.append(split[i]).append("号").append("及");
                    }
                }
            }
            monDetail.append("可用");
        }
        return monDetail.toString();
    }


    /**
     * 查询用户是否有可用券
     * @return
     */
    @PostMapping("/query/coupon/cust/usablePageList")
    public Response queryCouponUsable() throws Exception {
        Response response = new Response();
        Request request;
        CouponUsable couponUsable;
        request = getObject(CouponUsable.class);
        couponUsable = (CouponUsable) request.getData();

        /**验证参数**/
        this.validateService.validateForString(couponUsable.getCustId(), SysConstant.E01000010);
        this.validateService.validateForObject(couponUsable.getPageNum(), SysConstant.E01000011);
        this.validateService.validateForObject(couponUsable.getPageSize(), SysConstant.E01000004);

        /**先查询会员**/
        TblCustInf tblCustInf = this.custService.queryCustInfoByCustId(couponUsable.getCustId());
        if (tblCustInf == null) {
            throw new BusinessException(getErrorInfo(SysConstant.E01000036));
        }

        /**查询当前生效活动**/
        CardActivity cardActivity = new CardActivity();
        cardActivity.setValidDate(DateUtil.getCurrentLocalDateTime(DateUtil.DATE_FORMAT_COMPACT));
        cardActivity.setCardStatus(SysConstant.CARD_ACTIVITY_IS_OK);


        Map<String, CardActivityAndRule> cardActivityAndRuleMap = new HashMap<>();
        Object[] cardCode = this.cardActivityService.queryCardActivityAndRuleList(cardActivity).stream().map(s -> {
            cardActivityAndRuleMap.put(String.valueOf(s.getCardCode()), s);
            return s.getCardCode();
        }).toArray();
        cardActivity = null;


        List<CouponInfo> list = new ArrayList<>();
        ResCouponUsable resCouponUsable= new ResCouponUsable();
        if (cardCode == null) {
            resCouponUsable.setList(list);
            response.setData(resCouponUsable);
            response.setResCode(SysConstant.SYS_SUCCESS);
            return response;
        }

        /** 分页查询电子券 **/
        CardUser cardUser = new CardUser();
        cardUser.setCustId(tblCustInf.getCustId());
        cardUser.setStatus(SysConstant.CARD_USER_STATUS_0);
        cardUser.setValidCardCodes(cardCode);
        PageInfo<TblCardUser> pageInfo =  this.couponService.queryCouponPageList(cardUser,couponUsable.getPageNum(),couponUsable.getPageSize());
        cardUser = null;



        if (pageInfo != null) {
            BeanUtils.copyProperties(pageInfo, resCouponUsable);
            if(pageInfo.getSize() > 0){
                list = buildResponseVo(pageInfo.getList(), cardActivityAndRuleMap);
            }
        }
        resCouponUsable.setList(list);
        response.setData(resCouponUsable);
        response.setResCode(SysConstant.SYS_SUCCESS);
        return response;
    }


}
