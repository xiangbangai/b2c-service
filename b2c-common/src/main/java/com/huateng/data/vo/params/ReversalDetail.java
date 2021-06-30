package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReversalDetail {
    private String id;//子订单编号
    private BigDecimal number;//商品数量
    private BigDecimal totalPrice; //总积分数
}
