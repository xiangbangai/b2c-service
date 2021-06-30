package com.huateng.data.vo.params;

import lombok.Data;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/17
 * Time: 09:00
 * Description:
 */
@Data
public class AddPoints {

    private Integer requestType; //请求类型 0-正常交易 1-续传
    private String posId; //posId
    private String stationId; //油站编号
    private String shiftId; //班次
    private String listNo; //流水
    private String repairSerialNo; //续传流水
    private String businessDate; //营业日
    private String acctId; //客户属性
    private List<Goods> goods; //商品
    private List<PayInfo> payment; //支付信息
}
