package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/18
 * Time: 22:27
 * Description:
 */
@Data
public class PayInfo {
    private String payType; //支付方式
    private String subType; //二级卡类型
    private String payInfo; //支付信息
    private BigDecimal payValue; //支付金额
}
