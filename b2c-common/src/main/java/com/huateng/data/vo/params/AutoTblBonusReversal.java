package com.huateng.data.vo.params;

import com.huateng.data.model.db2.TblOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 积分冲正 兼容老交易
 */
@Data
public class AutoTblBonusReversal {
    private String orderId;//冲正订单id

    private String channel;

    private String targetOrderId;//目标订单id

    private Date hostDateTime;//主机时间

    private String businessDate;//营业日期

    private String posId;

    private String stationId;

    private Short reverseType;//冲正类型

    private String custId;//会员编号

    private BigDecimal reverseNumber;//冲正积分

    private List<ReversalDetail> details;//子订单

}
