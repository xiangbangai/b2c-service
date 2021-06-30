package com.huateng.data.vo.json.req;

import com.huateng.data.vo.params.ReversalDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BonusReversal {
    private Short reverseType;//0=整单冲正，1=部分冲正
    private String posId;
    private String stationId;
    private String targetSerialNo;//目标流水
    private String businessDate; //营业日期
    private String acctId;
    private String shiftId;
    private String listNo;
    private BigDecimal bonusNumber; //冲正总积分数
    private List<ReversalDetail> detail; //需要冲正的子订单
}
