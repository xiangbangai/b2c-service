package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblBonusDetail {
    private BigDecimal pkBonusDetail;

    private String custId;

    private String acctId;

    private String usageKey;

    private String txnType;

    private String txnCode;

    private String bonusCdFlag;

    private String bonusSsn;

    private String bpPlanType;

    private String validDate;

    private BigDecimal txnBonus;

    private BigDecimal validBonus;

    private BigDecimal bpValidBonus;

    private String activityId;

    private String ruleId;

    private String bonusSsnOra;

    private String txnCodeOra;

    private String txnDescOra;

    private BigDecimal txnAmtOra;

    private BigDecimal txnCntOra;

    private String txnDateOra;

    private String txnDate;

    private String txnTime;

    private String createDate;

    private String createTime;

    private String stlmDate;

    private String activityNm;

    private String ruleNm;

    private String channelNo;

    private String extCoulmn1;

    private String extCoulmn2;

    private String extCoulmn3;

    private BigDecimal extCoulmn4;

    private String returnFlag;

    private String stationId;

    private String posId;

    private String detailDesc;
}