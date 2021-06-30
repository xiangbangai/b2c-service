package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeResult {
    //当前总有效积分
    private BigDecimal totalBonus;
    //最近将到期积分
    private BigDecimal expBonus;
    //最近将到期的有效期
    private String expDate;
    
    private BigDecimal totalConsume; //使用积分
}
