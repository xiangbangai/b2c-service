package com.huateng.data.vo.params;

import lombok.Data;

@Data
public class AccountNoRegister {
    private String accountNo;
    private Integer isCustAgreement;//1=同意服务协议; 0=未同意服务协议
    private String agreementTime;//同意协议时间 yyyy-MM-dd HH:mm:ss
    private String mobile;
}
