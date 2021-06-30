package com.huateng.data.vo.params;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/13
 * Time: 16:21
 * Description:
 */
@Data
public class BonusPlanDetailParams {

    private String custId; //会员号
    private String bpPlanType; //积分计划
    private String expiredStatus; //过期状态 0-未过期 1-已过期
    private BigDecimal validBonus; //有效积分
    private Integer isOrderByDate; //1-desc排序 否则默认asc排序
    private List<String> validates; //指定日期
}
