package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ServiceOrder {
    private String id;

    private String channel;

    private String posId;

    private String stationId;

    private String reqSerialNo;

    private String repairSerialNo;

    private String targetSerialNo;

    private Date channelDate;

    private Date businessDate;

    private Date hostDate;

    private BigDecimal number;

    private BigDecimal returnableNumber;

    private BigDecimal orderPrice;

    private BigDecimal returnablePrice;

    private Short status; //0-处理中，1-成功，2-失败，3-被部分冲正，4-被全部冲正

    private Short orderType;//0-产生积分，1-兑换，2-冲正

    private BigDecimal validBefore;

    private Short operate;//0-减少，1-增加

    private BigDecimal validAfter;

    private String custId;

    private String acctId;

    private String mallId;
    private String shiftId;
    private String listNo;
}