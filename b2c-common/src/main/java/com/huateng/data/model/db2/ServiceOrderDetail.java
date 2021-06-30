package com.huateng.data.model.db2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ServiceOrderDetail {
    private String id;

    private String orderId;

    private String goodsId;

    private BigDecimal totalPrice; //【积分产生】为商品总价 【积分兑换】为商品总积分

    private BigDecimal unitPrice; //【积分产生】为商品单价  【积分兑换】为商品积分单价

    private String middleType;

    private String litType;

    private BigDecimal number;

    private BigDecimal returnableNumber;

    private String goodsName;

    private Short goodsType;

    private String discountType;

    private Short delivery;
    @JsonFormat(pattern="yyyy-MM-dd", timezone="GMT+8")
    private Date deliveryDate;

}