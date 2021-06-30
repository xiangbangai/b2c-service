package com.huateng.data.model.db2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ServiceOrderTxnInfo {
    private String id;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date hostDate;
    private BigDecimal number;
    private Short operate;//0-减少，1-增加
    private Short orderType;//0-产生积分，1-兑换，2-冲正
    private String stationId;
    private String stationName;
    private List<ServiceOrderDetail> orderDetails;
}