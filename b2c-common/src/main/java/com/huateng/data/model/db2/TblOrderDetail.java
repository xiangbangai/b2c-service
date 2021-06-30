package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblOrderDetail {
    private Integer pkOrderDtl;

    private String orderId;

    private String bpPlanType;

    private String custId;

    private String custLevel;

    private String cardLevel;

    private String custName;

    private String acctId;

    private String mchtId;

    private String goodsId;

    private String goodsNm;

    private BigDecimal orderNum;

    private BigDecimal orderBonus;

    private BigDecimal orderAmount;

    private String orderCancel;

    private String createTime;

    private String toRmsTime;

    private String returnTime;

    private String orderDetailId;
}