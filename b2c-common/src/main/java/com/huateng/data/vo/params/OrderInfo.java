package com.huateng.data.vo.params;

import lombok.Data;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/3/23
 * Time: 16:37
 * Description:
 */
@Data
public class OrderInfo {

    private String channel; //渠道
    private String reqSerialNo; //请求流水
    private Short status; //订单状态
    private String custId;
    private Short orderType; //订单类型
}
