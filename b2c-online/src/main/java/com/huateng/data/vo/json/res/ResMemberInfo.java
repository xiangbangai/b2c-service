package com.huateng.data.vo.json.res;

import com.huateng.data.model.db2.TblAcctInfPart;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2019/9/26
 * Time: 17:12
 * Description:
 */
@Data
public class ResMemberInfo {

    private String custId;
    private String mobile;
    private String bpPlanType;
    private String bpPlanTypeName;
    private BigDecimal totalBonus;
    private BigDecimal applyBonus;
    private BigDecimal expiredBonus;
    private BigDecimal validBonus;
    private String custBonusStatus;
    private BigDecimal expiredBonusThisYear;
    private String custInvoice;
    private List<TblAcctInfPart> acctList;
}
