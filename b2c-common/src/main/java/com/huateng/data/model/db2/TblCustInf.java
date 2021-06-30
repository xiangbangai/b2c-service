package com.huateng.data.model.db2;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TblCustInf {
    private String custId;

    private String custName;

    private String usageKey;

    private String openBrh;

    private String custType;

    private String custIdType;

    private String custIdNum;

    private String xuelii;

    private String custBirthday;

    private String openDate;

    private String closeDate;

    private String custMobile;

    private String custAddr;

    private String custLevel;

    private String custBonusStatus;

    private String modifyDate;

    private String modifyTime;

    private String extCoulmn1;

    private String extCoulmn2;

    private String extCoulmn3;

    private BigDecimal extCoulmn4;

    private String custGender;

    private String custMail;

    private String custCareer;

    private String plateNum;

    private String yearSettlementFlg;

    private String isAuthMobile;

    private String isAuthMail;

    private String isActive;

    private String observationDate;

    private Integer observationFuelCount;

    private Integer fuelCount;

    private String lastFuelDate;

    private String openId;

    private String passwd;

    private String custInvoice;

    private String bankNo;

    private Integer isChange;

    private Integer isAcceptEinvoice;

    private Integer isRealCust;

    private Integer isMobileUse;

    private Integer isPlateNumPay;

    private String activeDate;

    private String limitedType;

    private Integer limitedCount;

    private Integer limitedMaxCount;

    private String limitedModifyDate;

    private String limitedModifyUser;

    private Integer isCustAgreement;

    private String agreementTime;

    private String custHobby;

}