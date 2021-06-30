package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblFuelConsumption {
    private BigDecimal pkFuelConsumption;

    private String custId;

    private String goodsId;

    private String goodsNm;

    private BigDecimal goodsNum;

    private BigDecimal goodsTotalPrice;

    private BigDecimal goodsUnitPrice;

    private String txnDate;

    private String txnTime;

}