package com.huateng.data.vo.params;

import com.huateng.data.model.db2.TblCustInf;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ComputeBonus {
    private String channel; //渠道

    private String reqSerialNo; //请求流水

    private Date businessDate; //营业日期

    private Date reqDateTime;//请求时间

    private String acctId; //客户账号

    private TblCustInf custInf;//客户信息

    private String stationId; //油站编号

    private String posId; //posId

    private List<Goods> goods; //商品

    private List<PayInfo> payment; //支付信息
}
