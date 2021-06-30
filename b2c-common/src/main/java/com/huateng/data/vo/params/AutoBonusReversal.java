package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class AutoBonusReversal {
    private Date hostDateTime;//主机时间

    private String orderId;//冲正订单id

    private String targetOrderId;//目标订单id

    private Short reverseType;//冲正类型

    private String custId;//会员编号

    private BigDecimal reverseNumber;//冲正积分

    private List<ReversalDetail> details;//子订单
}
