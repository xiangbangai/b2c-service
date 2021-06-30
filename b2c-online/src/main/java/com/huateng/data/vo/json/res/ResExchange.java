package com.huateng.data.vo.json.res;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/6
 * Time: 16:26
 * Description:
 */
@Data
public class ResExchange {
    //当前总有效积分
    private BigDecimal totalBonus;
    //最近将到期积分
    private BigDecimal expBonus;
    //最近将到期的有效期
    private String expDate;
}
