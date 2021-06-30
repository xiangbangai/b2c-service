package com.huateng.data.vo.params;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddPointsResult {
    private String custId; //客户id

    private String taxName;//发票抬头

    private Integer isAcceptEInvoice;//是否接收自动开电子发票 0-不接受、1-接受

    private BigDecimal validBonus; //当前有效积分

    private BigDecimal txnBonus; //交易产生积分

    private BigDecimal willExpireBonus; //即将到期积分

    private String willExpireDate; //即将到期日期

    @JsonIgnore
    private SendWeChat delayMsg;//积分待发推送

    @JsonIgnore
    private SendWeChat genMsg;//积分产生推送
}
