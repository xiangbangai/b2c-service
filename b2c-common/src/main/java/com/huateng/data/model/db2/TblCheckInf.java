package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblCheckInf {
    private BigDecimal tblCheckInf;

    private String serviceid;

    private String organizationunitid;

    private String teller1;

    private String teller2;

    private String channel;

    private String bpPlanType;

    private String bpPlantYpeIn;

    private String custId;

    private String custIdIn;

    private String acctId;

    private String acctIdIn;

    private String adjustType;

    private String effectiveDate;

    private String validDate;

    private Long txnBonus;

    private String txnDesc;

    private String txnCode;

    private String txnExtInfo;

    private String chgRate;

    private String oprFlag;

    private String checkDesc;

    private String usageKey;

    private String createOper;

    private String createDate;

    private String createTime;

    private String modifyOper;

    private String modifyDate;

    private String modifyTime;

    private String extCoulmn1;

    private String extCoulmn2;

    private String extCoulmn3;

    private Long extCoulmn4;

    private String bussId;

    private String bussType;

    private String optType;

    private String batchFlag;

    private Long batchId;

    private String txnItems;

    private String stationId;

    private String bonusIntoType;

    private String adjustProperty;

    private String adjustOrderId;

}