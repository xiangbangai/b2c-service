package com.huateng.data.vo.params;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/6/24
 * Time: 16:30
 * Description:
 */
@Data
public class InvoiceInfo {

    private String invoiceStatus; //发票状态0已开票，1补打发票，2作废发票,3红冲发票,5未开发票 99开票中

    private Integer taxType; //已开发票类型 0表示纸质发票，1表示电子发票

    //发票开票信息
    private int invCon ;//发票内容类型：1-油品发票 2-非油品发票


    //发票开票信息
    private String category;//错误原因 ：开票类别代码：-1=未刷卡消费 0=消费时开具普通发票 1=充值时开具普通发票 2=月结开具普通发票 3=月结开具增值税发票 4=不开发票 5=售卡时开普通发票 6=收银员判断开票
    private String categoryDesc;//错误详情  开票类别说明

    private String payee;//收款人
    private String checker;//复核
    private String drawer;//开票人

    private BigDecimal amount;//未税金额合计
    private BigDecimal taxAmount;//税额合计
    private BigDecimal sumAmount ;//价税合计
    private String comment;//备注

    private String taxid; //纳税号
    private String bankname; //开户银行
    private String bankaccounts; //开户银行账号
    private String shopinfo; //开票名称
    private String shopaddr; //地址
    private String telno; //联系电话
    @JsonIgnore
    private String items;
}
