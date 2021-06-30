package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblBonusDelay {
    private String id;

    private String custId;

    private BigDecimal txnBonus;

    private String bonusCdFlag;

    private String stationId;

    private String createDate;

    private String createTime;

    private String createUser;

    private String status;

    private String validDate;

    private String adjustDesc;

    private String modifyDate;

    private String modifyTime;

    private String bonusSsn;

}