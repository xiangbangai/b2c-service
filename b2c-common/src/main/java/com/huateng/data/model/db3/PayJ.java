package com.huateng.data.model.db3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayJ {
    @JsonProperty("shopid")
    private String shopid;
    @JsonProperty("dt")
    private String dt;
    @JsonProperty("time")
    private String time;
    @JsonProperty("reqtime")
    private String reqtime;
    @JsonProperty("listno")
    private Integer listno;
    @JsonProperty("sublistno")
    private Integer sublistno;
    @JsonProperty("posId")
    private String posId;
    @JsonProperty("cashierId")
    private String cashierId;
    @JsonProperty("paySeq")
    private Short paySeq;
    @JsonProperty("payReason")
    private String payReason;
    @JsonProperty("payType")
    private String payType;
    @JsonProperty("deliverFlag")
    private String deliverFlag;
    @JsonProperty("currenCode")
    private String currenCode;
    @JsonProperty("payValue")
    private BigDecimal payValue;
    @JsonProperty("equivValue")
    private BigDecimal equivValue;
    @JsonProperty("cardno")
    private String cardno;
    @JsonProperty("flag3")
    private String flag3;
    @JsonProperty("trainflag")
    private String trainflag;
    @JsonProperty("serialid")
    private Integer serialid;
    @JsonProperty("shiftid")
    private Integer shiftid;
    @JsonProperty("workdate")
    private String workdate;
    @JsonProperty("pointsttl")
    private BigDecimal pointsttl;
    @JsonProperty("subcardtype")
    private String subcardtype;

}