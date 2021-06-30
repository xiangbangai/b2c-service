package com.huateng.data.vo.json.req;

import com.huateng.data.vo.params.ExchangeGoods;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Exchange {
    //营业日
    private String businessDate;
    //posId
    private String posId;
    //油站id
    private String stationId;
    //用户唯一属性
    private String acctId;
    //商城订单
    private String mallId;
    private String shiftId;
    private String listNo;
    //兑换商品
    private ArrayList<ExchangeGoods> goods;

}
