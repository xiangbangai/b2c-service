package com.huateng.data.vo.json.res;

import com.huateng.data.vo.params.AuditBonus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ResAuditPointsInfo {
    private String custId; //会员号

    private BigDecimal txnTotalBonus;//本次交易产生的积分

    private List<AuditBonus> bonusInfo;
}
