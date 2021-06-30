package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdjustOrder {
    private String serviceOrderId;//积分调整订单
    private String stationId;//
    private String custId;
    private String openId;
    private Short operate; //操作0=减分，1=加分
    private BigDecimal number;
    private String adjustProperty;//调整性质
    private String mallId;
    private String txnDesc;//推送信息
    private String txnItems;//调整事项
    private Date hostDate;
    private Date businessDate;
}
