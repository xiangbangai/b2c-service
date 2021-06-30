package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BonusEnterResult {
    private String custId; //会员id

    private BigDecimal validBonus;//总有效积分

    private BigDecimal txnBonus;//产生的总积分

    private BigDecimal expBonus;//今年快过期的积分

    private BigDecimal expDate;//有效期

    private List<MatchRule> list;
}
