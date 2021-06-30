package com.huateng.data.model.db2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class ServiceBonusDetail {
    private String orderId;

    private Integer orderSerial;

    private Short operate;

    private String validDate;

    private BigDecimal number;

    private BigDecimal returnableNumber;

    private String ruleId;
}