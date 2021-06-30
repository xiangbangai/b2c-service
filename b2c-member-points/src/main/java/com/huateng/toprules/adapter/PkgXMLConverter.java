package com.huateng.toprules.adapter;

import com.huateng.toprules.adapter.TxnBonusRulePkg.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.io.ByteArrayInputStream;

/**
 * PKG和XML的管理器
 * 
 * @author cheney
 * 
 */
public class PkgXMLConverter {

	/**
	 * Condition转换器
	 * 
	 * @author cheney
	 * 
	 */
	private static class ConditionConverter implements SingleValueConverter {
		public String toString(Object obj) {
			return ((Condition) obj).value;
		}

		public Object fromString(String value) {
			Condition c = new Condition();
			c.value = value;
			return c;
		}

		public boolean canConvert(Class type) {
			return type.equals(Condition.class);
		}
	}

	/**
	 * 得到XStream
	 * 
	 * @return
	 */
	private static XStream getXStream() {
		XStream xstream = new XStream();
		xstream.registerConverter(new ConditionConverter());

		xstream.alias("package", TxnBonusRulePkg.class);
		xstream.useAttributeFor(TxnBonusRulePkg.class, "packageName");
		xstream.aliasAttribute(TxnBonusRulePkg.class, "packageName", "name");
		xstream.addImplicitCollection(TxnBonusRulePkg.class, "activityGroups", Activity.class);

		xstream.alias("activity-group", Activity.class);
		xstream.useAttributeFor(Activity.class, "name");
		xstream.aliasAttribute(Activity.class, "name", "name");
		xstream.useAttributeFor(Activity.class, "periodStart");
		xstream.aliasAttribute(Activity.class, "periodStart", "period-start");
		xstream.useAttributeFor(Activity.class, "periodEnd");
		xstream.aliasAttribute(Activity.class, "periodEnd", "period-end");
		xstream.useAttributeFor(Activity.class, "validDate");
		xstream.aliasAttribute(Activity.class, "validDate", "valid-date");
		//add by zyk
		xstream.useAttributeFor(Activity.class, "effectiveDate");
		xstream.aliasAttribute(Activity.class, "effectiveDate", "effective-date");
		xstream.useAttributeFor(Activity.class, "effectiveFlag");
		xstream.aliasAttribute(Activity.class, "effectiveFlag", "effective-flag");
		
		xstream.useAttributeFor(Activity.class, "bonusType");
		xstream.aliasAttribute(Activity.class, "bonusType", "bonus-type");
		xstream.addImplicitCollection(Activity.class, "ruleGroups", RuleGroup.class);
		xstream.addImplicitCollection(Activity.class, "rules", Rule.class);

		xstream.alias("rule-group", RuleGroup.class);
		xstream.useAttributeFor(RuleGroup.class, "name");
		xstream.aliasAttribute(RuleGroup.class, "name", "name");
		xstream.useAttributeFor(RuleGroup.class, "conflictPolicy");
		xstream.aliasAttribute(RuleGroup.class, "conflictPolicy", "conflict-policy");
		xstream.useAttributeFor(RuleGroup.class, "priority");
		xstream.aliasAttribute(RuleGroup.class, "priority", "priority");
		xstream.addImplicitCollection(RuleGroup.class, "ruleGroups", RuleGroup.class);
		xstream.addImplicitCollection(RuleGroup.class, "rules", Rule.class);

		xstream.alias("rule", Rule.class);
		xstream.useAttributeFor(Rule.class, "name");
		xstream.aliasAttribute(Rule.class, "name", "name");
		xstream.useAttributeFor(Rule.class, "priority");
		xstream.aliasAttribute(Rule.class, "priority", "priority");
		xstream.aliasField( "conditions", Rule.class, "conditionGroups");
//		xstream.addImplicitCollection(Rule.class, "conditionGroups", ConditionGroup.class);
//
//		xstream.alias("conditions", ConditionGroup.class);
//		xstream.useAttributeFor(ConditionGroup.class, "type");
//		xstream.aliasAttribute(ConditionGroup.class, "type", "type");
//		xstream.addImplicitCollection(ConditionGroup.class, "conditions", Condition.class);

		xstream.alias("condition", Condition.class);

		xstream.alias("result", Result.class);
		xstream.aliasField("bonus-point", Result.class, "bonusPoint");
		return xstream;
	}

	/**
	 * XML转成pkg对象
	 * 
	 * @param content xml字符串
	 * @return
	 * @throws Exception
	 */
	public static TxnBonusRulePkg transXMLToPkg(String content) throws Exception {
		XStream xstream = getXStream();
		TxnBonusRulePkg pkg = (TxnBonusRulePkg) xstream.fromXML(new ByteArrayInputStream(content.getBytes()));
		return pkg;
	}

	/**
	 * pkg转成xml
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static String transPkgToXML(TxnBonusRulePkg pkg) throws Exception {
		XStream xstream = getXStream();
		String content = xstream.toXML(pkg);
		return content;
	}

}
