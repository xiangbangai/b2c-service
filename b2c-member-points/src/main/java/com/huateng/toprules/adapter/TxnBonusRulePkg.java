package com.huateng.toprules.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则包类
 * 
 * @author wbn
 * 
 */
public class TxnBonusRulePkg {
	/**
	 * 规则包名称
	 */
	public String packageName;

	/**
	 * 活动列表
	 */
	public List activityGroups = new ArrayList();// 活动列表

	/**
	 * 活动
	 * 
	 * @author wbn
	 * 
	 */
	public static class Activity {
		/**
		 * 名称
		 */
		public String name;
		
		public String funcKey;
		
		/**
		 * 有效期开始日期
		 */
		public String periodStart;
		
		/**
		 * 有效期结束如期
		 */		
		public String periodEnd;
		/**
		 * 积分生效日期，add by zyk
		 */
		public String effectiveDate;
		/**
		 * 生效标志
		 */
		public String effectiveFlag;
		
		/**
		 * 积分有效日期
		 */		
		public String validDate;
		
		/**
		 * 积分类型
		 */		
		public int bonusType;
		
		/**
		 * 规则组
		 */
		public List ruleGroups = new ArrayList();
		
		/**
		 * 规则
		 */
		public List rules = new ArrayList();
	}

	/**
	 * 规则组
	 * 
	 * @author wbn
	 * 
	 */
	public static class RuleGroup {
		/**
		 * 名称
		 */
		public String name;
		
		public String funcKey;
		
		/**
		 * 冲突策略
		 */
		public String conflictPolicy;
		
		/**
		 * 优先级
		 */
		public int priority;
		
		/**
		 * 规则组
		 */
		public List ruleGroups = new ArrayList();
		
		/**
		 * 规则
		 */
		public List rules = new ArrayList();
	}

	/**
	 * 规则
	 * 
	 * @author wbn
	 * 
	 */
	public static class Rule {
		/**
		 * 名称
		 */
		public String name;
		
		public String funcKey;
		
		/**
		 * 优先级
		 */
		public int priority;
		
		/**
		 * 条件列表
		 */
		public List conditionGroups = new ArrayList();// 条件列表
		
		/**
		 * 结果
		 */
		public Result result = new Result();// 积分结果
	}

	/**
	 * 条件组
	 * 
	 * @author cheney
	 * 
	 */
	public static class ConditionGroup {
		/**
		 * 条件
		 */
		public List conditions = new ArrayList();
		
		/**
		 * 类型
		 */
		public String type = "and";
	}

	/**
	 * 条件
	 * 
	 * @author cheney
	 * 
	 */
	public static class Condition {
		/**
		 * 值
		 */
		public String value;// 条件
	}

	/**
	 * 结果
	 * 
	 * @author cheney
	 * 
	 */
	public static class Result {
		/**
		 * 积分公式
		 */
		public String bonusPoint;
		
		/**
		 * 状态
		 */
		public String status;
	}
}
