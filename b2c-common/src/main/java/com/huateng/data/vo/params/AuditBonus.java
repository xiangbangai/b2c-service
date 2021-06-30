package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuditBonus {
    private String validDate;//有效期
    private BigDecimal txnBonus;//积分
}
