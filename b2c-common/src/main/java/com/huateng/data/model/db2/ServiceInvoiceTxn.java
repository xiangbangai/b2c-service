package com.huateng.data.model.db2;

import com.huateng.data.vo.params.SendInvoice;
import lombok.Data;

import java.util.Date;

@Data
public class ServiceInvoiceTxn {
    private String id;

    private String channel;

    private String reqSerialNo;

    private Date sendDate;

    private Date hostDate;

    private Integer count;

    private Short status;

    private String resultMsg;

    private String sendParams;

    private SendInvoice sendInvoice;

}