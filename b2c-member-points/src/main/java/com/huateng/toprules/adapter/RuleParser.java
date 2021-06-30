package com.huateng.toprules.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule工具类
 * 
 * @author cheney
 * 
 */
public class RuleParser {

	/**
	 * 基本类型，int, long, short, float, double, boolean, byte, char
	 */
	public static final int VARIABLE_PRIMARY_TYPE = 0;

	/**
	 * 字符串类型
	 */
	public static final int VARIABLE_STRING_TYPE = 1;
	

	/**
	 * 解析condition，返回为字符串数组 [0]为变量,[1]为操作, [2]为值
	 * 
	 * @param condition
	 * @return
	 */
	public static String[] parseCondition(String condition) {
		String regrex = "^\\s*(\\w+)\\s*(==|!=|>=|<=|>|<|not\\s+in|in)\\s*(.+)$";
		
		Pattern p = Pattern.compile(regrex);
		Matcher m = p.matcher(condition);
		while (m.find()) {
			int count = m.groupCount();
			String[] groups = new String[count];
			for (int i = 1; i <= count; i++) {
				groups[i - 1] = m.group(i);
			}
			return groups;
		}
		return null;
	}

	/**
	 * condition变成java代码
	 * 
	 * @param condition
	 * @return
	 */
	public static String condition2Java(String condition, int type, String objectName) {
		String[] cs = parseCondition(condition);
		
		//支付类型特殊处理
		if("pay_type".equalsIgnoreCase(cs[0])){
			return condition2JavaSet(condition, type, objectName);
		}
		
		
		String leftField = objectName + ".get" + cs[0].substring(0, 1).toUpperCase();
		if (cs[0].length() > 1) {
			leftField += cs[0].substring(1);
		}
		leftField += "()";
		String operator = cs[1];
		String value = cs[2];
		String returnValue;
		// 基本类型
		if (type == VARIABLE_PRIMARY_TYPE) {
			if (operator.equals("in") || operator.matches("not\\s+in")) {
				String newValue = value.replaceAll("\\(", "");
				newValue = newValue.replaceAll("\\)", "").trim();			
				String[] values = newValue.split(",");
				returnValue = leftField + " == " + values[0].trim();
				for (int i = 1; i < values.length; i++) {
					returnValue += " || " + leftField + " == " + values[i].trim();
				}
				if (operator.equals("in")) {
					return "(" + returnValue + ")";
				} else {
					return "!(" + returnValue + ")";
				}
			} else {
				return leftField + " " + operator + " " + value;
			}
		}
		// 字符串类型
		else if (type == VARIABLE_STRING_TYPE) {
			if (operator.equals("in") || operator.matches("not\\s+in")) {
				String newValue = value.replaceAll("\\(", "");
				newValue = newValue.replaceAll("\\)", "");				
				String[] values = newValue.split(",");
				returnValue = leftField + ".equals(" + values[0].trim() + ")";
				for (int i = 1; i < values.length; i++) {
					returnValue += " || " + leftField + ".equals(" + values[i].trim() + ")";
				}
				if (operator.equals("in")) {
					return "(" + returnValue + ")";
				} else {
					return "!(" + returnValue + ")";
				}
			} else if (operator.equals("==")) {
				return leftField + ".equals(" + value + ")";
			} else if (operator.equals("!=")) {
				return "!" + leftField + ".equals(" + value + ")";
			} else {
				returnValue = leftField + ".compareTo(" + value + ")";
				returnValue += " " + operator + " 0";		
				return returnValue;
			}
		}		
		return null;
	}

	/**
	 * condition变成java代码
	 * 
	 * @param condition
	 * @return
	 */
	public static String condition2JavaSet(String condition, int type, String objectName) {
		String[] cs = parseCondition(condition);
		String leftField = objectName + ".get" + cs[0].substring(0, 1).toUpperCase();
		if (cs[0].length() > 1) {
			leftField += cs[0].substring(1);
		}
		leftField += "Set()";
		String operator = cs[1];
		String value = cs[2];
		String returnValue;
		// 基本类型
		if (type == VARIABLE_PRIMARY_TYPE) {
			if (operator.equals("in") || operator.matches("not\\s+in")) {
				String newValue = value.replaceAll("\\(", "");
				newValue = newValue.replaceAll("\\)", "").trim();			
				String[] values = newValue.split(",");
				returnValue = leftField + " == " + values[0].trim();
				for (int i = 1; i < values.length; i++) {
					returnValue += " || " + leftField + " == " + values[i].trim();
				}
				if (operator.equals("in")) {
					return "(" + returnValue + ")";
				} else {
					return "!(" + returnValue + ")";
				}
			} else {
				return leftField + " " + operator + " " + value;
			}
		}
		// 字符串类型
		else if (type == VARIABLE_STRING_TYPE) {
			if (operator.equals("in") || operator.matches("not\\s+in")) {
				String newValue = value.replaceAll("\\(", "");
				newValue = newValue.replaceAll("\\)", "");				
				String[] values = newValue.split(",");
				returnValue = leftField + ".contains(" + values[0].trim() + ")";
				for (int i = 1; i < values.length; i++) {
					returnValue += " || " + leftField + ".contains(" + values[i].trim() + ")";
				}
				if (operator.equals("in")) {
					return "(" + returnValue + ")";
				} else {
					return "!(" + returnValue + ")";
				}
			} else if (operator.equals("==")) {
				return leftField + ".contains(" + value + ")";
			} else if (operator.equals("!=")) {
				return "!" + leftField + ".contains(" + value + ")";
			} else {
				returnValue = leftField + ".compareTo(" + value + ")";
				returnValue += " " + operator + " 0";		
				return returnValue;
			}
		}		
		return null;
	}
	
	
	
	/**
	 * 解析表达式
	 * 
	 * @param condition
	 * @return
	 */
	public static String[] parseExpression(String expression) {
		String regrex = "([_a-zA-Z]\\w*)";

		Pattern p = Pattern.compile(regrex);
		Matcher m = p.matcher(expression);
		List result = new ArrayList();
		while (m.find()) {
			result.add(m.group());
		}
		return (String[])result.toArray((Object[])new String[0]);
	}

	/**
	 * 表达式变成Java代码
	 * 
	 * @param condition
	 * @return
	 */
	public static String expressionToJava(String expression, String objectName) {
		String[] fields = parseExpression(expression);
		//这样拼保证唯一
		String replactValue = "";
		for (int i = 0; i < fields.length; i++) {
			replactValue += "_" + fields[i];
		}
		expression = expression.replaceAll("([_a-zA-Z]\\w*)", replactValue);
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			String objectField = objectName + ".get" + field.substring(0, 1).toUpperCase();
			if (field.length() > 1) {
				objectField += field.substring(1);
			}
			objectField += "()";
			expression = expression.replaceFirst(replactValue, objectField);
		}
		return expression;
	}

}
