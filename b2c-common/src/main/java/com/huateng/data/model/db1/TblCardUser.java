package com.huateng.data.model.db1;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TblCardUser {
    private String couponId; // 卡券唯一标识

    private Integer cardCode; // 卡券活动编号

    private String openId; // 微信open_id

    private String cardReceiveid; // 用户id

    private String createGetDate; // 领取日期

    private String createGetTime; // 领取时间

    private String useDate; // 卡卷使用日期

    private String useTime; // 卡卷使用时间

    private BigDecimal useMoney; // 实际抵扣金额

    private String cardStatus; // 卡券状态:0-未使用，1-使用中，2-已使用，3-已作废，4-已删除，5-已失效

    private String modifyDate; // 处理日期

    private String modifyTime; // 处理时间

    private String exp1; // pos唯一流水

    private String exp2; // 班结日期

    private String exp3; // 扩展字段3

    private String exp4; // 扩展字段4

    private String exp5; // 扩展字段5

    private String returnFlag; // 退货标志 1-已退货

    private String isLock; // 锁定状态 1-已锁定

    private Integer id; // 主键

    private String cardActive;

    private String activeChannel;

    private Date activeDateTime;

    private String startDate;

    private String endDate;

}