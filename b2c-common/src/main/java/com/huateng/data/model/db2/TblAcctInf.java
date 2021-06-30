package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblAcctInf {
    private BigDecimal pkAcctInf;

    private String custId;

    private String acctId;

    private String acctType;

    private String cardBank;

    private String cardPrd;

    private String extCoulmn1;

    private String extCoulmn2;

    private String extCoulmn3;

    private BigDecimal extCoulmn4;

    private String openId;

    private String createDate;

    private String createTime;

}