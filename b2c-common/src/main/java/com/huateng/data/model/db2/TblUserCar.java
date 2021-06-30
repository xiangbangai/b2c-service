package com.huateng.data.model.db2;

import lombok.Data;

@Data
public class TblUserCar {
    private String id;
    private String custId;
    private String plateNumber;
    private int plateNumberType;
    private String carFrameNum;
    private String engineNumber;
    private String carBrandName;
    private String price;
    private String displacement;
    private int oilType;
    private String logo;
    private String carSerialName;
    private int isDefault;
    private String status;
    private String openId;
    private String createTime;
    private String nonInductivePayType;
    private String defaultNonInductivePayType;
    private String licensePlateType;
}
