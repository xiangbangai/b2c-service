package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceCustLimit {
    private String custId;
    private Short limitType;
    private String limitKey;
    private BigDecimal limitValue;

}