package com.huateng.data.model.db3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleJ {
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
    @JsonProperty("waiterId")
    private String waiterId;
    @JsonProperty("vgno")
    private String vgno;
    @JsonProperty("goodsno")
    private String goodsno;
    @JsonProperty("placeno")
    private String placeno;
    @JsonProperty("groupno")
    private String groupno;
    @JsonProperty("deptno")
    private String deptno;
    @JsonProperty("amount")
    private Integer amount;
    @JsonProperty("colorsize")
    private String colorsize;
    @JsonProperty("itemValue")
    private BigDecimal itemValue;
    @JsonProperty("discValue")
    private BigDecimal discValue;
    @JsonProperty("itemType")
    private String itemType;
    @JsonProperty("vType")
    private String vType;
    @JsonProperty("discType")
    private String discType;
    @JsonProperty("authorizerId")
    private String authorizerId;
    @JsonProperty("x")
    private Integer x;
    @JsonProperty("deliverFlag")
    private String deliverFlag;
    @JsonProperty("flag1")
    private String flag1;
    @JsonProperty("flag2")
    private String flag2;
    @JsonProperty("flag3")
    private String flag3;
    @JsonProperty("trainflag")
    private String trainflag;
    @JsonProperty("price")
    private BigDecimal price;
    @JsonProperty("useGoodsno")
    private String useGoodsno;
    @JsonProperty("serialid")
    private Integer serialid;
    @JsonProperty("shiftid")
    private Integer shiftid;
    @JsonProperty("workdate")
    private String workdate;
    @JsonProperty("iszhongjinpay")
    private String iszhongjinpay;
    @JsonProperty("govprice")
    private BigDecimal govprice;
    @JsonProperty("govitemvalue")
    private BigDecimal govitemvalue;
    @JsonProperty("govdiscvalue")
    private BigDecimal govdiscvalue;
    @JsonProperty("points")
    private BigDecimal points;
    @JsonProperty("discvalueCard")
    private BigDecimal discvalueCard;
    @JsonProperty("fdcsseq")
    private Integer fdcsseq;
    @JsonProperty("fpnum")
    private Integer fpnum;
    @JsonProperty("taxrate")
    private BigDecimal taxrate;

}