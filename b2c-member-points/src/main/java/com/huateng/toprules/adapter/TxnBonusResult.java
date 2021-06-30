package com.huateng.toprules.adapter;

import cn.hutool.core.util.NumberUtil;

import java.math.BigDecimal;

/**
 * 积分结果
 * @author zsh
 *
 */
public class TxnBonusResult {
	public String ruleName;				;//规则名字
	public String bpPlanType		;//CHAR(4)	        "积分计划（规则生成的1000-通用卡消费积分计 1001-特殊卡消费积分计 1101-南航里程积分计划 1102-东航里程积分计划"
	public double   bonusPoint		;//BIGINT	        相关积分
	public String validDate		;//CHAR(8)	        有效期
	public String effectiveDate;//生效日期
	public String ruleConflict = "sum"	;	//        冲突策略:max大；min小；priority优先；sum汇总
	public int 		priority	;//优先级
	public boolean isExec = false;			//条件是否满足
	public boolean isEffect = false;			//是否生效(通过冲突策略)

	public String getBpPlanType() {
		return bpPlanType;
	}
	public void setBpPlanType(String bpPlanType) {
		this.bpPlanType = bpPlanType;
	}
	public double getBonusPoint() {
		return bonusPoint;
	}
	public void setBonusPoint(double bonus_point) {
		this.bonusPoint = bonus_point;
	}
	public String getValidDate() {
		return validDate;
	}
	public void setValidDate(String valid_date) {
		this.validDate = valid_date;
	}
	public String getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	/**
	 * 
	 * <p><strong>Description: </strong>四舍五入取积分</p>
	 * @author zyk
	 * @return
	 * @update 2013-10-23
	 */
	public BigDecimal getRoundBonusPoint() {
		/*double oriBonusPoint = bonusPoint;
		int intBonusPoint = (int)bonusPoint;
		if(oriBonusPoint-intBonusPoint>=0.5){
			return intBonusPoint+1;
		}else{
			return intBonusPoint;
		}*/
		//return  (int)bonusPoint;//业务要求直接取整

		//return bonusPoint;

		return NumberUtil.round(bonusPoint, 0, null);
	}
}
