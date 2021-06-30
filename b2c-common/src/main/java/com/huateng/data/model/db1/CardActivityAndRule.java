package com.huateng.data.model.db1;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardActivityAndRule {
    private Integer cardCode; // 卡券活动编号

    private String cardName; // 卡券名称

    private String cardTitel; // 卡券宣传语

    private String cardType; // 卡券类型-0：抵扣券1：折扣券

    private String cardScope; // 卡券范围-0：商城通用卷1：商户优惠劵

    private Integer cardTotalNum; // 申请总数

    private BigDecimal setMoney; // 优惠金额（如果是折扣卷，这为最大优惠金额）

    private Double cardDiscount; // 折扣比例

    private BigDecimal useImpose; // 使用限制金额（0为没有限制）

    private String cardSend; // 卡券是否可以转赠-0：否1：是

    private String cardSendtimes; // 卡券转增次数

    private String cardFrame; // 是否上卡架 0：否；1：是

    private String cardRepeat; // 是否可重复领取-0：否1：是

    private Integer repeatTimes; // 重复领取次数

    private String cardStartGetDate; // 卡券开始领取日期

    private String cardEndGetDate; // 卡卷结束领取日期

    private Integer cardGetNum; // 卡卷已领取数量（初始值为0）

    private String cardBegintime; // 卡券有效期起始日期

    private String cardEndtime; // 卡券有效期截止日期

    private String createOper; // 创建人

    private String createDate; // 创建日期

    private String createTime; // 创建时间

    private String checkOper; // 审批人

    private String checkDate; // 审批日期

    private String checkTime; // 审批时间

    private String checkOpinion; // 审批意见

    private String cardStatus; // 卡券状态（卡券状态0- 待审核 1- 审核通过 2- 未通过 3- 已结束 4- 删除 5- 申请中 6-停用 7-启用待审核 8-停用待审核）

    private String exp1; // 电子券属性标识（0-油品券；1-非油品券；2-油非混合券）

    private String exp2; // 扩展字段2

    private String exp3; // 扩展字段3

    private String exp4; // 扩展字段4

    private String exp5; // 扩展字段5

    private String useRule; // 优惠券使用规则

    private String makeSncode; // 是否已经生产SN码-0：否1：是

    private String cardBelongto; // 电子券归属方

    private String activityTitel; // 活动说明

    private String workregionId; // 所属小区

    private String workregionName; // 所属小区名

    private String stationId; // 所属油站

    private String stationName; // 所属油站名

    private String allocationRatio; // 电子券分摊比例

    private String allocationRatioOil; // 电子券分摊比例油品

    private String allocationRatioNotOil; // 电子券分摊比例非油品

    private Integer cardUseNum; // 卡卷已使用数量（初始值为0）


    //---------------------------------------------- 活动规则
    private String ruleWorkregionId; // 所属小区id

    private String ruleWorkregionName; // 所属小区名称

    private String ruleStationId; // 所属油站id

    private String ruleStationName; // 所属油站名称

    private String couponChannel; // 电子券使用渠道

    private String goodsMidId; // 商品中类

    private String goodsTId; // 商品小类

    private BigDecimal txnMoneyOil; // 油品消费金额

    private String payType; // 电子券支付方式

    private String couponContent; // 电子券内容

    private String couponExplain; // 提货券折扣定义

    private Integer txnNumberOil; // 油品消费数量

    private BigDecimal txnMoneyNotOil; // 非油品消费金额

    private Integer txnNumberNotOil; // 非油品消费数量

    private String goodsId; // 商品id

    private Integer txnNumberMixOil; // 油非混合消费数量

    private BigDecimal txnMoneyMixOil; // 油非混合消费金额

    private String dayOfWeek; // 星期几

    private String dayOfMon; // 每月几日

    private String startTime1; // 开始时间1

    private String endTime1; // 结束时间1

    private String startTime2; // 开始时间2

    private String endTime2; // 结束时间2

}
