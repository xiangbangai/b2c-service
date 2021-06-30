package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TblOrder {
    private String orderId;

    private String chnlNo;

    private String stationId;

    private String posId;

    private String acqSsn;

    private String txnDate;

    private String txnTime;

    private String refTxnSsn;

    private String oraOrderId;

    private String custName;

    private String acctId;

    private BigDecimal orderBonus;

    private BigDecimal orderAmount;

    private String orderTime;

    private String orderStatus;

    private String orderDesc;

    private String checkType;

    private String orderCust;

    private String orderAddr;

    private String orderPhone;

    private String orderCancel;

    private String createTime;

    private String orderPost;

    private String custId;

    private String bpPlanType;

    private String bonusSsn;

    private String toRmsTime;

    private String returnTime;

    private String txnCode;//交易类型

    private String bonusCDFlag;

}