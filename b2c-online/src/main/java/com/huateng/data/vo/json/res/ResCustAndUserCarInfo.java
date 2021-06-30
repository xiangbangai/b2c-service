package com.huateng.data.vo.json.res;

import lombok.Data;

@Data
public class ResCustAndUserCarInfo {
    private String acctId;
    private String acctName;
    private String openId;
    private String uid;
    private String nonInductivePayType;
    private String defaultNonInductivePayType;
    private String oilType;
    private String licensePlateType;
}
