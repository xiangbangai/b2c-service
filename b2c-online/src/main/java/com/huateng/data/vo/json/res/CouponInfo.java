package com.huateng.data.vo.json.res;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponInfo {

    private String cardId; // couponId卡券唯一标识 tblCardUser.couponId

    private String cardName; // 卡券名称 tblCardActivity.cardName

    private Integer cardCode; // 卡券活动编号 tblCardUser.cardCode 或者 tblCardActivity.cardCode

    private String cardGetDate; //电子券领取日期 tblCardUser.createGetDate

    private String cardStatus; // 卡券状态:0-未使用，1-使用中，2-已使用，3-已作废，4-已删除，5-已失效 tblCardUser.cardStatus

    private String cardType; // 卡券类型-0：抵扣券1：折扣券 2:商品券", tblCardActivity.cardType

    private BigDecimal setMoney; // 优惠金额（如果是折扣卷，这为最大优惠金额） tblCardActivity.setMoney

    private Double cardDiscount; // 折扣比例 tblCardActivity.cardDiscount

    private BigDecimal useImpose; // 使用限制金额（0为没有限制） tblCardActivity.useImpose

    private String stationId; // 所属油站 tblCardActivity.stationId

    private String cardBeginDate; //"电子券有效开始日期", tblCardActivity.cardBeginTime

    private String cardEndDate;  //:"电子券有效结束日期", tblCardActivity.cardEndTime

    private String activityTitel; // 活动说明 tblCardActivity.activityTitel

    private BigDecimal txnMoneyOil; // 油品消费金额 tblActivityUseRule.txnMoneyOil

    private Integer txnNumberOil; // 油品消费数量 tblActivityUseRule.txnNumberOil

    private BigDecimal txnMoneyNotOil; // 非油品消费金额  tblActivityUseRule.txnMoneyNotOil

    private Integer txnNumberNotOil; // 非油品消费数量 tblActivityUseRule.txnNumberNotOil

    private String dayOfWeek; // 星期几

    private String dayOfMon; // 每月几日

}
