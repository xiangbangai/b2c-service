package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MatchRule {
    private String ruleName;//规则名

    private BigDecimal ruleTxnBonus;//积分

    private String validDate;//有效期
}
