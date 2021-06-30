package com.huateng.data.model.db1;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblActivityUseRule {
    private Integer cardCode; // 活动id

    private String workregionId; // 所属小区id

    private String workregionName; // 所属小区名称

    private String stationId; // 所属油站id

    private String stationName; // 所属油站名称

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