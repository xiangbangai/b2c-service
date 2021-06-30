package com.huateng.toprules.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 包结果类
 * @author wbn
 *
 */
public class PackageResult {
	public String pkgName;		//包名
//	public List pkgResults = new ArrayList();//活动结果列表
	
	@SuppressWarnings("unchecked")
	public List activityGroupResults = new ArrayList();
	
//	public TxnBonusResult result = new TxnBonusResult();
	
	public static class ActivityResult{
		public String name;
		public String periodStart;
		public String periodEnd;
		public String validDate;
		//add by zyk
		public String effectiveDate;
		public String bonusType;
		public String effectiveFlag;
		
		public TxnBonusResult result = new TxnBonusResult();
		
		@SuppressWarnings("unchecked")
		public List results = new ArrayList();
		@SuppressWarnings("unchecked")
		public List ruleGroupResults = new ArrayList();
	}
	
	public static class RuleGroupResult{
		public String name;
		public String conflictPolicy;
		public int priority;
		public TxnBonusResult result = new TxnBonusResult();
		public List results = new ArrayList();
		public RuleGroupResult ruleGroupResult = null;//嵌套(不能在这里new,会死循环)
		public RuleGroupResult(){
		}
		public RuleGroupResult(String name){
			this.name = name;
		}
	}
	
	void  printResult(String prefix,StringBuffer buff,TxnBonusResult result){
		buff.append(prefix+"结果"+result.ruleName+"--->isExec="+result.isExec+",isEffect="+result.isEffect+",bpPlanType="+result.bpPlanType+",bonusPoint="+(long)result.bonusPoint+",ruleConflict="+result.ruleConflict+ ",effectiveDate="+result.effectiveDate+  ",validDate="+result.validDate+",priority="+result.priority+"\n");
	}
	
	void  printGrpResult(String prefix,StringBuffer buff,RuleGroupResult v_result){
		if(v_result!=null){
			buff.append(prefix+"组信息--->name="+v_result.name+",conflictPolicy="+v_result.conflictPolicy+",priority="+v_result.priority+"\n");
			printResult(prefix,buff,v_result.result);
			if(v_result.results!=null){
				int size = v_result.results.size();
				for(int i= 0;i<size;i++){
					TxnBonusResult rt= (TxnBonusResult) v_result.results.get(i);
					printResult("\t"+prefix,buff,rt);
				}
			}
			printGrpResult("\t\t"+prefix,buff,v_result.ruleGroupResult);
		}
	}
	
	
	public void dump(){
		StringBuffer buff = new StringBuffer();
//		if(this.pkgResults!=null){
//			buff.append("规则包"+this.pkgName+"结果：\n");
//			int size = this.pkgResults.size();
//			int i = 0;
//			for(i=0;i<size;i++){
//				TxnBonusResult rt = (TxnBonusResult) this.pkgResults.get(i);
//				printResult("",buff,rt);
//			}			
//		}
//		printResult("pkg=",buff,result);
		int i = 0;
		int size = this.activityGroupResults.size();
		for(i=0;i<size;i++){
			ActivityResult ar = (ActivityResult) this.activityGroupResults.get(i);
			if(ar!=null){
				printActResult("\t活动"+ar.name+"--->",buff,ar);
				int j = 0;
				int sizej =  ar.results.size();
				for(j=0;j<sizej;j++){
					TxnBonusResult rt = (TxnBonusResult) ar.results.get(j);
					buff.append("\t活动独立规则结果--->\n");
					printResult("\t\t",buff,rt);
				}
				if(ar.result!=null){
					buff.append("\t活动结果--->\n");
					printResult("\t\t",buff,ar.result);
					
					if(ar.ruleGroupResults!=null){
						if(ar.ruleGroupResults.size()>0){
							int k = 0;
							int sizek = ar.ruleGroupResults.size();
							for(k=0;k<sizek;k++){
								RuleGroupResult grp = (RuleGroupResult) ar.ruleGroupResults.get(k);
								buff.append("\t\t每个组--->\n");
								printGrpResult("\t\t\t",buff,grp);
							}
						}
					}
				}
			}

		}
		System.out.println(buff.toString());
	}

	private void printActResult(String prefix,StringBuffer buff, ActivityResult ar) {
		buff.append(prefix+"bonusType="+ar.bonusType+",name="+ar.name+",periodEnd="+ar.periodEnd+",periodStart="+ar.periodStart+ ",活动effectiveDate="+ar.effectiveDate +  ",活动effectiveFlag="+ar.effectiveFlag+ ",活动validDate="+ar.validDate+"\n");
	}

//	public List getPkgResults() {
//		return pkgResults;
//	}
//
//	public void setPkgResults(List pkgResults) {
//		this.pkgResults = pkgResults;
//	}

	public List getActivityGroupResults() {
		return activityGroupResults;
	}

	public void setActivityGroupResults(List activityGroupResults) {
		this.activityGroupResults = activityGroupResults;
	}

//	public TxnBonusResult getResult() {
//		return result;
//	}
//
//	public void setResult(TxnBonusResult result) {
//		this.result = result;
//	}
}
