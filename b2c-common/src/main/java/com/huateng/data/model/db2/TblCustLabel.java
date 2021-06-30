package com.huateng.data.model.db2;

import lombok.Data;

@Data
public class TblCustLabel {
    private String id;

    private String custId;

    private String labelId;

    private String createDate;

    private String createTime;

    private String createUser;

    private String status;

    private String batchId;

    private String effDate;

    private String expDate;

    private String labelType;

    private Integer labelLevel;

    private String usableDate;

    private Integer validDays;

    private String renewalLog;
}