package com.huateng.toprules.adapter;

import com.huateng.toprules.adapter.TxnBonusRulePkg.Activity;
import com.huateng.toprules.adapter.TxnBonusRulePkg.Condition;
import com.huateng.toprules.adapter.TxnBonusRulePkg.Rule;
import com.huateng.toprules.adapter.TxnBonusRulePkg.RuleGroup;
import com.huateng.toprules.util.RuleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动生成规则的类
 * 
 * @author wbn
 * 
 */
public class CodeGenner {

	public final static String ResultClassName = "TxnBonusResult";
	public final static String BSSClassName = "TxnBonus";
	public static int	   funcNameId = 10;
	@SuppressWarnings("unchecked")
	public static Map fieldTypeMap = new HashMap();
	/**
	 * 是否试算
	 */
	public boolean isTest = false;
	@SuppressWarnings("unchecked")
	public CodeGenner(){
		/*字段类型map*/
		
		
		fieldTypeMap.put("usage_key",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_date",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_time",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("brithday",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_week_date",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_month",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_date_month",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("cust_id",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("acct_id",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("acct_type",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("station_id",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("station_name",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("machine_id",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_type",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("goods_id",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("goods_name",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("goods_mid_type",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("is_include_refueling",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("is_include_goods",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("channel",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_ssn_ora",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("txn_date_ora",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("cust_level",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("is_brith_day",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("is_brith_month",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("is_brith_week",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("goods_lit_type",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("field1",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("field2",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("field3",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("field4",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("field5",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("marketregion",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("goods_count",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		fieldTypeMap.put("goods_ids",new Integer(RuleParser.VARIABLE_STRING_TYPE));
		fieldTypeMap.put("pay_type",new Integer(RuleParser.VARIABLE_STRING_TYPE));


		fieldTypeMap.put("txn_amt",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		fieldTypeMap.put("goods_num",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		fieldTypeMap.put("goods_unit_price",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		fieldTypeMap.put("goods_act_price",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		fieldTypeMap.put("goods_act_total_price",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		fieldTypeMap.put("cust_level_coefficient",new Integer(RuleParser.VARIABLE_PRIMARY_TYPE));
		
	}
	
	String getCheckValidateFuncName() {
		return "_checkValidate";
	}

	String getActivityFuncName(Activity act) {
		return act.funcKey;
	}

	private String getRuleGroupFuncName(Activity act, RuleGroup grp) {
		return grp.funcKey;
	}

	private String getRuleFuncName(TxnBonusRulePkg pkg, Activity act, RuleGroup grp, Rule rule) {
		return rule.funcKey;
	}

	private String getConflictPolicyFuncName() {
		return "_checkConflictPolicy";
	}

	void declearConflictPolicyFunc(StringBuffer buff) {
		buff.append("function " + ResultClassName + " " + getConflictPolicyFuncName() + "(String plicy,List arg){\n");
		buff.append("\t" + ResultClassName + " result = new " + ResultClassName + "();\n\t");
		buff.append("\tdouble tmpValue=0L;\n\t");
		buff.append("result.isExec = false;\n\t");
		buff.append("\t" + ResultClassName + " resultTmp = null;\n\t");
		
//		buff.append("\tresultTmp=(" + ResultClassName + ")arg.get(0);\n\t");
//		
//		buff.append("\ttmpValue+=resultTmp.bonusPoint;\n\t");
//		buff.append("\tresultTmp.isEffect=false;\n\t");
		
		buff.append("if(plicy.equals(\"max\")){\n\t");
		
		buff.append("\tint size=arg.size();\n\t");
		buff.append("\tfor(int i=0;i<size;i++){\n\t");

		buff.append("\t\t" + ResultClassName + " rs = (" + ResultClassName + ")arg.get(i);\n");
		buff.append("\t\t\tif(resultTmp == null && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp=rs;\n\t");
		buff.append("\t\t}\n");
		
		buff.append("\t\t\tif(resultTmp!=null && rs.bonusPoint>=resultTmp.bonusPoint && resultTmp.isExec==true && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp = rs;\n\t");
		buff.append("\t\t\trs.isEffect = false;\n\t");
		buff.append("\t\t\ttmpValue = rs.bonusPoint;\n\t");
		buff.append("\t\t}\n\t");		
		
		buff.append("\t}\n\t");
		
		buff.append("}else if(plicy.equals(\"min\")){\n\t");
	
		buff.append("\tint size=arg.size();\n\t");
		buff.append("\tfor(int i=0;i<size;i++){\n\t");

		buff.append("\t\t" + ResultClassName + " rs = (" + ResultClassName + ")arg.get(i);\n");
		buff.append("\t\t\tif(resultTmp == null && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp=rs;\n\t");
		buff.append("\t\t}\n");
	
		buff.append("\t\t\tif(resultTmp!=null && rs.bonusPoint<=resultTmp.bonusPoint && resultTmp.isExec==true && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp = rs;\n\t");
		buff.append("\t\t\trs.isEffect = false;\n\t");
		buff.append("\t\t\ttmpValue = rs.bonusPoint;\n\t");
		buff.append("\t\t}\n\t");
		
		buff.append("\t}\n\t");
		
		buff.append("}else if(plicy.equals(\"sum\")){\n\t");

		buff.append("\tint size=arg.size();\n\t");
		buff.append("\tfor(int i=0;i<size;i++){\n\t");
		buff.append("\t\t" + ResultClassName + " rs = (" + ResultClassName + ")arg.get(i);\n");
		
		buff.append("\t\t\tif(resultTmp == null && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp=rs;\n\t");
		buff.append("\t\t}\n");
		
		buff.append("\t\t\tif(resultTmp !=null && resultTmp.isExec==true && rs.isExec==true){\n\t");

		buff.append("\t\t\ttmpValue+=rs.bonusPoint;\n\t");
		buff.append("\t\tresultTmp = rs;\n\t");
		buff.append("\t\t\trs.isEffect = true;\n\t");
		buff.append("\t\t}\n\t");
		
		buff.append("\t}\n\t");


		buff.append("}else if(plicy.equals(\"priority\")){\n\t");

		buff.append("\tint size=arg.size();\n\t");
		buff.append("\tfor(int i=0;i<size;i++){\n\t");
		
		buff.append("\t\t" + ResultClassName + " rs = (" + ResultClassName + ")arg.get(i);\n");
		buff.append("\t\t\tif(resultTmp == null && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp = rs;\n\t");
		buff.append("\t\t}\n");
		
		buff.append("\t\t\tif(resultTmp!=null && rs.priority<=resultTmp.priority && resultTmp.isExec==true && rs.isExec==true){\n\t");
		buff.append("\t\t\tresultTmp = rs;\n\t");
		buff.append("\t\t\ttmpValue = rs.bonusPoint;\n\t");
		buff.append("\t\t\trs.isEffect = true;\n\t");
		buff.append("\t\t}\n\t");
		
		buff.append("\t}\n\t");
				
		
		buff.append("}\n\t");
		
		buff.append("if(resultTmp!=null){\n\t");
		buff.append("\tresult.bpPlanType = resultTmp.bpPlanType;\n\t");
		buff.append("\tresult.validDate = resultTmp.validDate;\n\t");
		
		//add by zyk
		buff.append("\tresult.effectiveDate = resultTmp.effectiveDate;\n\t");
		
		
		buff.append("\tresult.ruleConflict = resultTmp.ruleConflict;\n\t");
		buff.append("\tresult.ruleName = resultTmp.ruleName;\n\t");
		buff.append("\tresult.priority = resultTmp.priority;\n\t");
//		buff.append("		System.out.println(\"laoboo2\");\n");
		buff.append("\tresult.bonusPoint=tmpValue;\n\t");
		buff.append("\tresult.isEffect=true;\n\t");
		buff.append("\tresultTmp.isEffect=true;\n\t");
		buff.append("\tresult.isExec=true;\n\t");
		buff.append("\tresultTmp.isExec=true;\n\t");
		buff.append("}\n\t");
		
		buff.append("return result;\n");
		buff.append("}\n");
	}
	
	void declearCopyResultFunc(StringBuffer buff){
		buff.append("function TxnBonusResult copyResult(TxnBonusResult src,TxnBonusResult des){\n");
		buff.append("\tdes.bpPlanType = src.bpPlanType;\n");
		buff.append("\tdes.bonusPoint = src.bonusPoint;\n");
		buff.append("\tdes.validDate = src.validDate;\n");
		//add by zyk
		buff.append("\tdes.effectiveDate = src.effectiveDate;\n");
		
		
		
//		buff.append("\tdes.ruleConflict = src.ruleConflict;\n");
		buff.append("\tdes.isExec = src.isExec;\n");
		buff.append("\tdes.isEffect = src.isEffect;\n");
//		buff.append("\tdes.priority = src.priority;\n");
//		buff.append("\tdes.ruleName = src.ruleName;\n");
		buff.append("\treturn des;\n");
		buff.append("}\n\n");
	}
/*	
	void declearDateFunc(StringBuffer buff){
		buff.append("function String topRules_getDate(String txnDate,int num,int type){\n");
			buff.append("Date d3;\n");
			buff.append("Calendar c = Calendar.getInstance();\n");
			buff.append("DateFormat df = new SimpleDateFormat(\"yyyyMMdd\");\n");
			buff.append("java.util.Date date= df.parse(txnDate);\n");
			buff.append("c.setTime(date);\n");

			buff.append("if(type==0){\n");//*"M0",当月月底；"Mn",第n月月底
				buff.append("c.add(Calendar.MONTH, num);\n");
				buff.append("Date d2 = c.getTime();\n");
				buff.append("c.setTime(d2);\n");
				
				//*获得月底的日期
				buff.append("c.add(Calendar.MONTH,1);\n");
				buff.append("c.set(Calendar.DATE,1);\n");
				buff.append("c.add(Calendar.DATE,-1);\n");
				buff.append("d3 = c.getTime();\n");
				buff.append("return df.format(d3);\n");
			buff.append("}\n");
		
			buff.append("if(type==1){\n");//*"D0",代表当天所在月份的月底；"Dn"，代表第n天所在月份的月底
				//*获得第Ｎ天后的日期
				buff.append("c.add(Calendar.DATE, num);\n");
				buff.append("Date d2 = c.getTime();\n");
				buff.append("c.setTime(d2);\n");
				
				//*获得月底的日期
				buff.append("c.add(Calendar.MONTH,1);\n");
				buff.append("c.set(Calendar.DATE,1);\n");
				buff.append("c.add(Calendar.DATE,-1);\n");
				buff.append("d3 = c.getTime();\n");
				buff.append("return df.format(d3);\n");
			buff.append("}\n");
		
			buff.append("if(type==2){\n");//* "d0",代表当天；"dn",代表第n天
				//*获得第Ｎ天后的日期
			
				buff.append("c.add(Calendar.DATE, num);\n");
				buff.append("Date d2 = c.getTime();\n");
				buff.append("return df.format(d2);\n");
			buff.append("}\n");
		buff.append("return \"\";\n");
		buff.append("}\n\n");
	}
*/
	
	void declearDateFunc(StringBuffer buff){
		
		buff.append("function String topRules_getDate(String txnDate,String dateExp){\n");
		//交易日期长度应该为8位yyyyMMdd
//		buff.append("	System.out.println(txnDate+\"laoboo\");System.out.println(dateExp+\"laoboo\");\n");
		buff.append("	if(txnDate.length()>8){\n");
		buff.append("		txnDate = txnDate.substring(0,8);\n");
		buff.append("	}\n");
		buff.append("	if(\"td\".equals(dateExp)){\n");
		buff.append("		return txnDate;\n");
		//小企业积分生效日期为："20110101" -laoboo
		buff.append("	} else if(\"20110101\".equals(dateExp)){\n");
		buff.append("		return \"20110101\";\n");
		buff.append("	}\n");
			
		buff.append("	Calendar c = Calendar.getInstance();\n");
		buff.append("	DateFormat df = new SimpleDateFormat(\"yyyyMMdd\");\n");
		buff.append("	Date date = df.parse(txnDate);\n");
		buff.append("	c.setTime(date);\n");
		buff.append("	if(dateExp.startsWith(\"Y\")){\n");
//		buff.append("		System.out.println(\"laoboo1\");\n");
		buff.append("		if(dateExp.indexOf(\"M\")>0){\n");//M代表取月的最后一天
//		buff.append("		System.out.println(\"laoboo2\");\n");
		buff.append("			int yearCount=Integer.parseInt(dateExp.substring(1,dateExp.indexOf(\"M\")));\n");
		buff.append("			int monCount=Integer.parseInt(dateExp.substring(dateExp.indexOf(\"M\")+1));\n");
		buff.append("			c.add(Calendar.YEAR, yearCount);\n");
		buff.append("			c.set(Calendar.MONTH, monCount);\n");
		buff.append("			c.set(Calendar.DATE,1);\n");
		buff.append("			c.add(Calendar.DATE,-1);\n");
		buff.append("		}else if(dateExp.indexOf(\"m\")>0){\n");//m代表取月的第一天
//		buff.append("		System.out.println(\"laoboo3\");\n");
		buff.append("			int yearCount=Integer.parseInt(dateExp.substring(1,dateExp.indexOf(\"m\")));\n");
		buff.append("			int monCount=Integer.parseInt(dateExp.substring(dateExp.indexOf(\"m\")+1));\n");
		buff.append("			c.add(Calendar.YEAR, yearCount);\n");
		buff.append("			c.set(Calendar.MONTH, monCount-1);\n");
		buff.append("			c.set(Calendar.DATE,1);\n");
		buff.append("		}\n");
		buff.append("	}else if(dateExp.startsWith(\"M\")){\n");
//		buff.append("		System.out.println(\"laoboo4\");\n");
		buff.append("		int monCount=Integer.parseInt(dateExp.substring(1));\n");
		buff.append("		c.add(Calendar.MONTH, monCount+1);\n");
		buff.append("		c.set(Calendar.DATE,1);\n");
		buff.append("		c.add(Calendar.DATE,-1);\n");
		buff.append("	}\n");
		//积分生效日期按月处理-laoboo
		buff.append("	 else if(dateExp.startsWith(\"m\")){\n");
		buff.append("		int monCount=Integer.parseInt(dateExp.substring(1));\n");
		buff.append("		c.add(Calendar.MONTH, monCount);\n");
		buff.append("		c.set(Calendar.DATE,1);\n");
//		buff.append("		System.out.println(\"laoboo5 \"+monCount);\n");
		buff.append("	}\n");
		buff.append("	Date returnDate = c.getTime();\n");
		buff.append("	return df.format(returnDate);\n");
		buff.append("}\n\n");
	}
	
	
	void declearActivityFunc(TxnBonusRulePkg pkg, Activity act, StringBuffer buff) {
		buff.append("function ActivityResult " + getActivityFuncName(act) + "(TxnBonus arg){\n");
		// buff.append("\tif("+getCheckValidateFuncName()+"(\""+act.periodStart+"\",\""+act.periodEnd+"\",arg.getTxn_date())==false"+"){\n\t\t");
		buff.append("\tActivityResult actResult = new ActivityResult();\n");

		
		buff.append("\tif(" + getCheckValidateFuncName() + "(\"" + act.periodStart + "\",\"" + act.periodEnd
				+ "\",arg.getTxn_date())==false" + "){\n\t\t");
		buff.append("\tactResult.result.isExec=false;\n");
		buff.append("return actResult;\n");
		buff.append("\t}\n");
		int i = 0;

		buff.append("\tList results = new ArrayList();\n");
		buff.append("\tList results4act = new ArrayList();\n");
		buff.append("\tList grpResults = new ArrayList();\n");
		
		if (act.ruleGroups != null) {
			int size = act.ruleGroups.size();
			int j = act.ruleGroups.size();
			for (j=0;j<size;j++) {
				RuleGroup group = (RuleGroup) act.ruleGroups.get(j);
				@SuppressWarnings("unused")
				int d = i++;

				newResult("resultTmp"+group.name,buff,pkg,act,group,null);
				
				buff.append("\tRuleGroupResult grpResult"+group.name+" = new RuleGroupResult(\""+group.name+"\");\n");
//				buff.append("\tgrpResult.result = "+getRuleGroupFuncName(act, group)+ "(arg,grpResult.result);\n");
			
				newResult("grp_result"+group.name,buff,pkg,act,group,null);
				buff.append("\tgrpResult"+group.name+".result = grp_result"+group.name+";\n");
				buff.append("\tgrpResult"+group.name+".result="+getRuleGroupFuncName(act, group)+ "(arg,grpResult"+group.name+");\n");
				
				buff.append("\tgrpResult"+group.name+".conflictPolicy = \""+group.conflictPolicy+"\";\n");
				buff.append("\tgrpResult"+group.name+".name = \""+group.name+"\";\n");
				buff.append("\tgrpResult"+group.name+".priority = Integer.parseInt(\""+group.priority+"\");\n");

				buff.append("\tgrpResults.add(grpResult"+group.name+");\n");
				
				buff.append("\tresults.add(copyResult(grpResult"+group.name+".result,resultTmp"+group.name+"));\n");
				//buff.append("\tresults.add(resultTmp);\n");
			}
		}

		if (act.rules != null) {
			int size = act.rules.size();
			int j = 0;
			for (j=0;j<size;j++) {
				Rule rule = (Rule) act.rules.get(j);
				int d = i++;
				buff.append("\t" + ResultClassName + " result" + (d) + " = " + getRuleFuncName(pkg, act,null, rule)
						+ "(arg);\n");
				buff.append("\tresults.add(result" + (d) + ");\n");
				buff.append("\tresults4act.add(result" + (d) + ");\n");
			}
		}
		
		buff.append("\tactResult.ruleGroupResults = grpResults;\n");/*活动规则组的结果*/
		
		buff.append("\tactResult.results = results4act;\n");/*活动规则的结果*/
		 
		buff.append("\tcopyResult(" + getConflictPolicyFuncName() + "(\"" + "sum" + "\",results),actResult.result);\n");

	
		buff.append("\tactResult.name = \""+act.name+"\";\n");
		buff.append("\tactResult.periodStart = \""+act.periodStart+"\";\n");
		buff.append("\tactResult.periodEnd = \""+act.periodEnd+"\";\n");
		buff.append("\tactResult.validDate = \""+act.validDate+"\";\n");
		//add by zyk
		buff.append("\tactResult.effectiveDate = \""+act.effectiveDate+"\";\n");
		
		buff.append("\tactResult.bonusType = \""+act.bonusType+"\";\n");
		
		buff.append("\tactResult.effectiveFlag = \""+act.effectiveFlag+"\";\n");
		
		buff.append("\tactResult.result.ruleName=\""+act.name+"\";\n");
		//buff.append("\tactResult.result.validDate=\""+act.validDate+"\";\n");
		buff.append("\tactResult.result.bpPlanType=\""+act.bonusType+"\";\n");		
		if(!isTest){
			buff.append("\tactResult.ruleGroupResults = null;\n");/*非试算*/
			buff.append("\tif(actResult.result.isExec==false || actResult.result.isEffect==false)\n{\t");
			buff.append("\tactResult.result=null;\n");/*试算*/
			buff.append("\t}\n\t");/*试算*/
		}else{
			buff.append("\tactResult.results = results4act;\n");/*活动规则的结果*/
		}		
		buff.append("\treturn actResult;\n");/*整个包的结果*/
		buff.append("}\n\n");
	}
	
	
	String newResult(String name, StringBuffer buff, TxnBonusRulePkg pkg, Activity act, RuleGroup grp, Rule rule){
		buff.append("\n\tTxnBonusResult "+name+" = new TxnBonusResult();\n");
		buff.append("\t"+name+".ruleConflict = \""+((grp==null)?"sum":grp.conflictPolicy)+"\";\n");
		buff.append("\t"+name+".validDate = \""+act.validDate+"\";\n");
		
		//add by zyk
		buff.append("\t"+name+".effectiveDate = \""+act.effectiveDate+"\";\n");
		
		
		
		buff.append("\t"+name+".bpPlanType = \""+act.bonusType+"\";\n");
		buff.append("\t"+name+".isExec = false;\n");
		
		if(rule!=null){
			buff.append("\t"+name+".priority = "+rule.priority+";\n");
			buff.append("\t"+name+".ruleName = \""+rule.name+"\";\n");
		}else{
			if(grp!=null){
				buff.append("\t"+name+".priority = "+grp.priority+";\n");
				buff.append("\t"+name+".ruleName = \""+grp.name+"\";\n");
			}else{
				buff.append("\t"+name+".ruleName = \""+pkg.packageName+"\";\n");
				buff.append("\t"+name+".priority = 0;\n");
			}
		}
		buff.append("\n");
		return buff.toString();
	}

	@SuppressWarnings("unchecked")
	List declearRuleGroupFunc(TxnBonusRulePkg pkg, Activity act, RuleGroup grp) {
		List funcLists = new ArrayList();
		StringBuffer buff = new StringBuffer();

		buff.append("function " + ResultClassName + " " + getRuleGroupFuncName(act, grp) + "(TxnBonus arg,RuleGroupResult v_result){\n");
		
		buff.append("\tList results = new ArrayList();\n");
		newResult("result",buff,pkg,act,grp,null);
		
		int i = 0;

		if (grp.ruleGroups != null) {
			int size = grp.ruleGroups.size();
			int j = 0;
			for (j=0;j<size;j++) {
				RuleGroup group = (RuleGroup) grp.ruleGroups.get(j);
				@SuppressWarnings("unused")
				int d = (i++);
				
				List funcListsTmp = declearRuleGroupFunc(pkg, act, group);
				funcLists.remove(funcListsTmp);
				funcLists.addAll(funcListsTmp);
				
				newResult("resultTmp"+group.name,buff,pkg,act,group,null);
				buff.append("\tRuleGroupResult grpResult"+group.name+" = new RuleGroupResult();\n");
				newResult("grp_result"+group.name,buff,pkg,act,group,null);
				buff.append("\tgrpResult"+group.name+".result = grp_result"+group.name+";\n");
				buff.append("\tgrpResult"+group.name+".result="+getRuleGroupFuncName(act, group)+ "(arg,grpResult"+group.name+");\n");
				
				buff.append("\tgrpResult"+group.name+".conflictPolicy = \""+group.conflictPolicy+"\";\n");
				buff.append("\tgrpResult"+group.name+".name = \""+group.name+"\";\n");
				buff.append("\tgrpResult"+group.name+".priority = Integer.parseInt(\""+group.priority+"\");\n");
//				buff.append("\tgrpResults.add(grpResult"+group.name+");\n");
				buff.append("\tresults.add(copyResult(grpResult"+group.name+".result,resultTmp"+group.name+"));\n");
				
				buff.append("\tv_result.ruleGroupResult=grpResult"+group.name+";\n");
				
//				buff.append("\t" + ResultClassName + " result" + (d) + " = " + getRuleGroupFuncName(act, group)
//						+ "(arg,result"+d+");\n");
//				buff.append("\tresults.add(result" + (d) + ");\n");
			}
		}

		if (grp.rules != null) {
			int j = 0;
			int size = grp.rules.size();
			for (j=0;j<size;j++) {
				Rule rule = (Rule) grp.rules.get(j);
				int d = (i++);
				
				StringBuffer buffTmp = new StringBuffer();
				String ruleFunc = declearRuleFunc(pkg, act, grp, rule, buffTmp);
				funcLists.remove(ruleFunc);
				funcLists.add(ruleFunc);
				
				
				buff.append("\t" + ResultClassName + " result" + (d) + " = " + getRuleFuncName(pkg, act, grp,rule)
						+ "(arg);\n");
				buff.append("\tresults.add(result" + (d) + ");\n");
			}
		}
		
		buff.append("\tresult = " + getConflictPolicyFuncName() + "(\"" + grp.conflictPolicy + "\",results);\n");
		if(isTest){
			buff.append("\tv_result.results = results;\n");
		}else{
			buff.append("\tv_result.results = null;\n");
		}
		buff.append("\tresult.ruleName=\""+grp.name+"\";\n");
		buff.append("\treturn result;\n");

		buff.append("}\n\n");
		funcLists.remove(buff.toString());
		funcLists.add(buff.toString());
		return funcLists;
	}
	
	public String getDRL(TxnBonusRulePkg pkg) {
		return getDRL(pkg,false);
	}

	@SuppressWarnings("unchecked")
	public String getDRL(TxnBonusRulePkg pkg, boolean isTest) {
		this.isTest = isTest;
		StringBuffer buff = new StringBuffer();
		List funs = new ArrayList();
		
		/*申明头*/
		declearHander(pkg,buff);

		/*生成函数名*/
		makeFuncKey(pkg);
		
		/*生成公共函数*/
		declearCopyResultFunc(buff);
		declearCheckValidFunc(buff);
		declearConflictPolicyFunc(buff);
		declearDateFunc(buff);

		if (pkg.activityGroups != null) {
			int size = pkg.activityGroups.size();
			int i = 0;
			for (i=0;i<size;i++) {
				Activity act = (Activity) pkg.activityGroups.get(i);
				if (act.rules != null) {
					int sizej = act.rules.size();
					int j = 0;
					for (j=0;j<sizej;j++) {
						Rule rule = (Rule) act.rules.get(j);
						declearRuleFunc(pkg, act, null, rule, buff);
					}
				}
				
				if (act.ruleGroups != null) {
					int sizek = act.ruleGroups.size();
					int k = 0;
					for (k=0;k<sizek;k++) {
						RuleGroup grp = (RuleGroup) act.ruleGroups.get(k);
						List funcLists = declearRuleGroupFunc(pkg, act, grp);

						int sizel = funcLists.size();
						int l = 0;
						for (l=0;l<sizel;l++) {
							String strTmp = (String) funcLists.get(l);
							if (!funs.contains(strTmp)) {
								buff.append(strTmp);
							}
						}
					}
				}
				
				declearActivityFunc(pkg, act, buff);
			}
		}


		
		if (pkg.activityGroups != null) {
			int size = pkg.activityGroups.size();
			int i = 0;
			for (i=0;i<size;i++) {
				Activity act = (Activity) pkg.activityGroups.get(i);
				buff.append("\nrule \"rule_" + act.name + "_\"\n");
				buff.append("enabled true\n");
				buff.append("no-loop true\n");
				buff.append("when\n");
				buff.append("$p : " + BSSClassName + "( )\n");
				buff.append("then\n");
				buff.append("$p.result.pkgName=\""+pkg.packageName+"\";\n");
				buff.append("ActivityResult actResult= "+getActivityFuncName(act) + "($p);\n");
				buff.append("if(actResult!= null){\n\t");
				buff.append("$p.result.activityGroupResults.add(actResult);\n");
				buff.append("}\n");
				
				buff.append("end\n\n");
			}
			
			
		}
		return buff.toString();
	}

	/**
	 * 规则函数
	 * @param pkg
	 * @param act
	 * @param grp
	 * @param rule
	 * @param buff
	 * @return
	 */
	private String declearRuleFunc(TxnBonusRulePkg pkg, Activity act, RuleGroup grp, Rule rule, StringBuffer buff) {
		buff.append("function " + ResultClassName + " " + getRuleFuncName(pkg, act,grp, rule) + "(" + BSSClassName
				+ " arg){\n\t");
//		buff.append("" + ResultClassName + " result=new TxnBonusResult();\n");
		newResult("result",buff,pkg,act,grp,rule);
		String condition = "";

		@SuppressWarnings("unused")
		int j = 0;
		@SuppressWarnings("unused")
		boolean bool = true;

		if (rule.conditionGroups != null && rule.conditionGroups.size() > 0) {
			int i = 0;
			int size = rule.conditionGroups.size();
			int ii = 0;
			for (ii=0;ii<size;ii++) {
				Condition cdtion = (Condition) rule.conditionGroups.get(ii);
				i++;
				if (cdtion.value != null) {
					System.out.println("]]]]>>>"+cdtion.value);
					String conditions[] = RuleParser.parseCondition(cdtion.value);
//					System.out.println(conditions[0]+" "+conditions[1]+" "+conditions[2]);
					condition += RuleParser.condition2Java(cdtion.value, ((Integer) CodeGenner.fieldTypeMap.get(conditions[0])).intValue(), "arg");
					if (i != rule.conditionGroups.size()) {
						condition += " && ";
						bool = true;
					} else {
						bool = false;
					}
				}
			}
		}


		
		if (condition != null && !condition.equals("")) {
			buff.append("\tif(" + condition + "){\n");
			
			//buff.append("\tint valid_date=" + act.validDate + ";\n");
			
			buff.append("\tint txnDate_year=Integer.parseInt(arg.getTxn_date().substring(0,4));\n");
			buff.append("\tint txnDate_Month=Integer.parseInt(arg.getTxn_date().substring(4,6));\n");
			buff.append("\tint txnDate_day=Integer.parseInt(arg.getTxn_date().substring(6,8));\n");
		
			if(act.validDate.equals("20991230")){//1.1 "20991230",无需计算，直接取值"20991230",代表永久有效；
				buff.append("\tresult.validDate= \"20991230\";\n");
			}else{// Fix By Zyk
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),\""+act.validDate.trim()+"\");\n");
			}
			
			buff.append("\tresult.effectiveDate= topRules_getDate(arg.getTxn_date(),\""+act.effectiveDate.trim()+"\");\n");
			
			/* Fix By Zyk
			 else if(act.validDate.equals("Y0")){//"Y0",当年年底，取当前日期的年份"yyyy"+"1230";"
				buff.append("\tresult.validDate= \"\"+txnDate_year+"+"\"1230\""+";\n");
			}else if(act.validDate.startsWith("Y")){//"Yn",第n年年底，取当前日期的年份"yyyy+n"+"1230";
				int txnDate_yearn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= \"\"+(txnDate_year+" + txnDate_yearn+ ")+"+"\"1230\""+";\n");
			}else if(act.validDate.startsWith("M")){//"Yn",第n年年底，取当前日期的年份"yyyy+n"+"1230";
				int txnDate_monthn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),"+txnDate_monthn+",0);\n");
			}else if(act.validDate.startsWith("D")){//"D0",代表当天所在月份的月底；"Dn"，代表第n天所在月份的月底；
				int txnDate_dayn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),"+txnDate_dayn+",1);\n");
			}else if(act.validDate.startsWith("d")){//"d0",代表当天；"dn",代表第n天
				int txnDate_dayn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),"+txnDate_dayn+",2);\n");
			}
*/
//			if(act.validDate.equals("0")){
//				buff.append("\tresult.validDate= \"29991230\";\n");
//			}else{
//				buff.append("\tint txnDate_year=Integer.parseInt(arg.getTxn_date().substring(0,4))+valid_date-1;\n");
//				buff.append("\tresult.validDate= \"\"+txnDate_year+"+"\"1230\""+";\n");
//			}
//			"r_EBNK00000002R000002";
			buff.append("\t result.bonusPoint=" + RuleParser.expressionToJava(rule.result.bonusPoint,"arg") + ";\n");
//			buff.append("\t BigDecimal decimalValue = new BigDecimal(Double.toString(result.bonusPoint));\n");
//			buff.append("\t BigDecimal decimalReslut = new BigDecimal(\"1\");\n");
//			buff.append("\t result.bonusPoint = decimalValue.divide(decimalReslut,0,BigDecimal.ROUND_HALF_UP).doubleValue(); \n");
			buff.append("\t\tresult.isExec = true;\n");
			
			buff.append("\t}else{\n");
			buff.append("\t\tresult.isExec = false;\n");
			buff.append("\t\tresult.isEffect = false;\n");
			buff.append("\t}\n");
		}else{/*没定义条件*/

			buff.append("\tint txnDate_year=Integer.parseInt(arg.getTxn_date().substring(0,4));\n");
			buff.append("\tint txnDate_Month=Integer.parseInt(arg.getTxn_date().substring(4,6));\n");
			buff.append("\tint txnDate_day=Integer.parseInt(arg.getTxn_date().substring(6,8));\n");
			if(act.validDate.equals("20991230")){//1.1 "20991230",无需计算，直接取值"20991230",代表永久有效；
				buff.append("\tresult.validDate= \"20991230\";\n");
			}else{// Fix By Zyk
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),\""+act.validDate.trim()+"\");\n");
			}
			buff.append("\tresult.effectiveDate= topRules_getDate(arg.getTxn_date(),\""+act.effectiveDate.trim()+"\");\n");
			
			/*else if(act.validDate.equals("Y0")){//"Y0",当年年底，取当前日期的年份"yyyy"+"1230";"
				buff.append("\tresult.validDate= \"\"+txnDate_year+"+"\"1230\""+";\n");
			}else if(act.validDate.startsWith("Y")){//"Yn",第n年年底，取当前日期的年份"yyyy+n"+"1230";
				int txnDate_yearn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= \"\"+(txnDate_year+" + txnDate_yearn+ ")+"+"\"1230\""+";\n");
			}else if(act.validDate.startsWith("M")){//"Yn",第n年年底，取当前日期的年份"yyyy+n"+"1230";
				int txnDate_monthn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),"+txnDate_monthn+",0);\n");
			}else if(act.validDate.startsWith("D")){//"D0",代表当天所在月份的月底；"Dn"，代表第n天所在月份的月底；
				int txnDate_dayn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),"+txnDate_dayn+",1);\n");
			}else if(act.validDate.startsWith("d")){//"d0",代表当天；"dn",代表第n天
				int txnDate_dayn=Integer.parseInt(act.validDate.substring(1,act.validDate.length()));
				buff.append("\tresult.validDate= topRules_getDate(arg.getTxn_date(),"+txnDate_dayn+",2);\n");
			}*/
//			buff.append("		System.out.println(\"laoboo1\");\n");
			buff.append("\tresult.bonusPoint=" + RuleUtil.convertExpression(rule.result.bonusPoint) + ";\n");
//			buff.append("\tresult.validDate=\"" + act.validDate + "\";\n");
//			buff.append("\tresult.bpPlanType=\"" + act.bonusType + "\";\n");
//			if (grp != null && grp.conflictPolicy != null && !grp.conflictPolicy.equals("")) {
//				buff.append("\tresult.ruleConflict=\"" + grp.conflictPolicy + "\";\n");
//			}
			buff.append("\t\tresult.isExec = true;\n");
		}

		buff.append("\treturn result;\n");
		buff.append("}\n\n");
		return buff.toString();
	}

	/**
	 * 检查活动有效期函数
	 * @param buff
	 */
	private void declearCheckValidFunc(StringBuffer buff) {
		buff.append("function boolean " + getCheckValidateFuncName()
				+ "(String startDate,String endDate,String date){\n\t");
		buff.append("if(date.length()==8){\n");
		buff.append("\t\tdate+=\"000000\";\n");
		buff.append("\t}\n");
		buff.append("return (Long.parseLong(startDate)<= Long.parseLong(date) && Long.parseLong(date) <= Long.parseLong(endDate));\n");
		buff.append("}\n\n");
	}

	private void declearHander(TxnBonusRulePkg pkg, StringBuffer buff) {
		buff.append("package "+pkg.packageName+"\n");
		buff.append("import com.huateng.toprules.adapter." + ResultClassName + ";\n");
		buff.append("import com.huateng.toprules.adapter." + BSSClassName + ";\n");
		buff.append("import com.huateng.toprules.adapter.PackageResult;\n");
		buff.append("import com.huateng.toprules.adapter.PackageResult.ActivityResult;\n");
		buff.append("import com.huateng.toprules.adapter.PackageResult.RuleGroupResult;\n");
		buff.append("import java.util.List;\n");
		buff.append("import java.util.ArrayList;\n");
		buff.append("import java.util.Map;\n");
		buff.append("import java.util.HashMap;\n");
		buff.append("import org.drools.WorkingMemory;\n");
		buff.append("import org.drools.RuleBase;\n");
		buff.append("import java.util.Collection;\n");
		buff.append("import java.util.Date;\n");
		buff.append("import java.util.Calendar;\n");
		buff.append("import java.text.DateFormat;\n");
		buff.append("import java.text.SimpleDateFormat;\n");

	}
	
	
	private void makeRuleFuncKey(String prefix, Rule v_rule){
		v_rule.funcKey="_b_"+Integer.toHexString(funcNameId++)+"_"+v_rule.name.replaceAll(" ", "_")+"_"+prefix;
	}
	
	
	private void makeRuleGrpFuncKey(String prefix, RuleGroup v_grp){
		v_grp.funcKey="_a_"+Integer.toHexString(funcNameId++)+"_"+v_grp.name+"_"+prefix;
		if (v_grp.ruleGroups != null) {
			int size = v_grp.ruleGroups.size();
			int i = 0;
			for (i=0;i<size;i++) {
				RuleGroup grp = (RuleGroup) v_grp.ruleGroups.get(i);
				makeRuleGrpFuncKey(v_grp.funcKey,grp);
			}
		}
		if (v_grp.rules != null) {
			int size = v_grp.rules.size();
			int i = 0;
			for (i=0;i<size;i++) {
				Rule rule = (Rule) v_grp.rules.get(i);
				makeRuleFuncKey(v_grp.funcKey,rule);
			}
		}
	}
	
	private void makeFuncKey(TxnBonusRulePkg pkg){
		if (pkg.activityGroups != null) {
			int size = pkg.activityGroups.size();
			int i = 0;
			for (i=0;i<size;i++) {
				Activity act = (Activity) pkg.activityGroups.get(i);
				act.funcKey = act.name;
				if (act.ruleGroups != null) {
					int sizej = act.ruleGroups.size();
					int j = 0;
					for (j=0;j<sizej;j++) {
						RuleGroup grp = (RuleGroup) act.ruleGroups.get(j);
						makeRuleGrpFuncKey(act.funcKey,grp);
					}
				}
				
				if (act.rules != null) {
					int sizej = act.rules.size();
					int j = 0;
					for (j=0;j<sizej;j++) {
						Rule rule = (Rule) act.rules.get(j);
						makeRuleFuncKey(act.funcKey,rule);
					}
				}
			}
		}
	}
	
}
