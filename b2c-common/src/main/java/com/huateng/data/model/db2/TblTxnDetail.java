package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblTxnDetail {
    private BigDecimal pkTxnDetail;

    private String usageKey;

    private String acqSsn;

    private String bonusSsn;

    private String keyReversal;

    private String chnlNo;

    private String txnDate;

    private String txnTime;

    private String txnType;

    private String txnCode;

    private String txnDesc;

    private String txnExtInfo;

    private String custId;

    private String acctId;

    private String bpPlanType;

    private String custIdRef;

    private String acctIdRef;

    private String bpPlanTypeRef;

    private BigDecimal txnBonus;

    private String bonusCdFlag;

    private String txnMchtNo;

    private String brhId;

    private String replyCode;

    private String txnStatus;

    private String replyMessage;

    private String createDate;

    private String createTime;

    private String orderId;

    private String oprUser;

    private String checkUser;

    private BigDecimal extCoulmn4;

    private String extCoulmn3;

    private String extCoulmn2;

    private String extCoulmn1;

    private String oraTxnDate;

    private String returnFlag;

    private String stationId;

    private String posId;

    private String oraKeyReversal;
}