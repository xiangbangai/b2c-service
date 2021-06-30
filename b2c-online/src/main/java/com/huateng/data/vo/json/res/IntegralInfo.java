package com.huateng.data.vo.json.res;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/26
 * Time: 17:43
 * Description:
 */
@Data
public class IntegralInfo {

    private String bpPlanType;
    private String bpPlanTypeName;
    private BigDecimal totalBonus;
    private BigDecimal applyBonus;
    private BigDecimal expiredBonus;
    private BigDecimal validBonus;
    private String custBonusStatus;
    private BigDecimal expiredBonusThisYear;
}
