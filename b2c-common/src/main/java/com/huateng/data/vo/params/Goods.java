package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/18
 * Time: 17:02
 * Description:
 */
@Data
public class Goods {

    private String id; //唯一编号
    private String goodsId; //商品编号
    private BigDecimal totalPrice; //总价格
    private BigDecimal unitPrice; //单价
    private Integer middleType; //中类
    private Integer litType; //小类
    private BigDecimal number; //数量
    private String goodsName; //商品名称
    private Short goodsType; //类型 0-非油品 1-油品 2-手工录入油品
    private String discountType; //折扣类型
}
