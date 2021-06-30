package com.huateng.data.vo.json.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustPoints {
    private String custId; //会员id
    private String stationId; //油站编号
    private String posId; //POS编号
    private String businessDate;//营业日
    private String shiftId; //班次（商城可为空）
    private String listNo; //单据号（商城可为空）
    private String mallId; //订单号
    private BigDecimal number; //调整积分
    private String pushMsg; //推送内容
    private String adjustName;//调整事项
    private String property; //调整性质0=现场消费，1=非现场交易 （商城传1）
}
