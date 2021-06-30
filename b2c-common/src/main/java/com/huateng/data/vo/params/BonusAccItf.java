package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class BonusAccItf {
	private String activityId;//满足活动

	private String ruleId;//满足规则

	private String goodsId;//商品编号

	private String goodsName;//商品名称

	private BigDecimal txnBonus;//积分

	private String validDate;//有效期

	private String bpPlanType;//积分计划

	private BigDecimal txnAmtOra;//原交易金额
}
