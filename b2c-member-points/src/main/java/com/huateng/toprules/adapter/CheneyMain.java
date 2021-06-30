package com.huateng.toprules.adapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class CheneyMain {
	public static void main1(String args[]) throws Exception {

		File file = new File("d:\\rule.xml");
		InputStream in = new FileInputStream(file);
		byte[] bytes = new byte[4096];
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int len = in.read(bytes);
		while (len != -1) {
			stream.write(bytes, 0, len);
			len = in.read(bytes);
		}
		String content = new String(stream.toByteArray(), "utf-8");
		//System.out.println(content);
		
		TxnBonusRulePkg pkg = PkgXMLConverter.transXMLToPkg(content);

		String content2 = PkgXMLConverter.transPkgToXML(pkg);
		System.out.println(content2);

	}
	
//	public static void main(String args[]) {
//		String str = "name not    in ( \"北京\"  ,  \"上海\"  )";
//		String str2 = "name != \"cheney\"";
//		String str3 = "b > \"20101219\"";
//		String[] group = RuleParser.parseCondition(str);
//		System.out.println(group[0]);
//		System.out.println(group[1]);
//		System.out.println(group[2]);
//		System.out.println(RuleParser.condition2Java(str, 1, "abc"));
//		System.out.println(RuleParser.condition2Java(str2, 1, "abc"));
//		System.out.println(RuleParser.condition2Java(str3, 1, "abc"));
//
//		String expression = "3 *(ab3_33c + def)/c";
//		group = RuleParser.parseExpression(expression);
//		System.out.println(group[0]);
//		System.out.println(group[1]);
//		System.out.println(group[2]);
//		System.out.println(RuleParser.expressionToJava(expression, "abc"));
//
//	}

}
