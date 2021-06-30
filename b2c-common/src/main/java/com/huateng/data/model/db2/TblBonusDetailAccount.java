package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblBonusDetailAccount {
    private BigDecimal id;

    private String custId;

    private String bonusSsn;

    private String stationId;

    private String workDate;

    private String shiftId;

    private String posId;

    private String listNo;

    private String goodsId;

    private String goodsLitType;

    private BigDecimal goodsNum;

    private BigDecimal goodsPrice;

    private String bonusType;

    private BigDecimal txnBonus;

    private BigDecimal txnBonusPrice;

    private String ruleStationId;

    private String ruleId;

    private String goodsType;

}