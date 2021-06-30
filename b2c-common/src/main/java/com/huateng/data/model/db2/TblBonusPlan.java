package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblBonusPlan {
    private BigDecimal pkBonusPlan;

    private String usageKey;

    private String custId;

    private String acctId;

    private String lockStatus;

    private String bpPlanType;

    private BigDecimal totalBonus;

    private BigDecimal validBonus;

    private BigDecimal applyBonus;

    private BigDecimal expireBonus;

    private String createOper;

    private String createDate;

    private String createTime;

    private String modifyOper;

    private String modifyDate;

    private String modifyTime;

    private String extCoulmn1;

    private String extCoulmn2;

    private String extCoulmn3;

    private BigDecimal extCoulmn4;

    private BigDecimal validBonus2;

    private String expiredStatus;

}