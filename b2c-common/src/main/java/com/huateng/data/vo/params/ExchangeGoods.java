package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeGoods {
    //唯一id
    private String id;
    //商品编号
    private String goodsId;
    //商品名称
    private String goodsName;
    //积分总价
    private BigDecimal totalPrice;
    //积分单价
    private BigDecimal unitPrice;
    //兑换数量
    private BigDecimal number;
    //折扣类型
    private String discountType;

}
