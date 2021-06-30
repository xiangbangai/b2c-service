package com.huateng.toprules.adapter;

import com.huateng.toprules.SessionProvider;
import com.huateng.toprules.SessionProviderFactory;
import com.huateng.toprules.StatefulRuleSession;
import com.huateng.toprules.util.PackageUtil;
import org.drools.compiler.DroolsParserException;
import org.drools.rule.Package;

import java.io.*;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class TopRules implements Serializable{
	private String pkgDir = "./";
	private String pkgBackupDir = "./";
	private boolean debug = true;
	private boolean isTest = false;

	/**
	 * @param pkgDir
	 *            生成的规则包存放目录
	 * @param pkgBackupDir
	 *            生成的规则包备份目录
	 * @param isTest
	 *            是否试算包
	 */
	public TopRules(String pkgDir, String pkgBackupDir, boolean isTest) {
		this.pkgDir = pkgDir;
		this.pkgBackupDir = pkgBackupDir;
		this.isTest = isTest;
	}

	public TopRules(){
		
	}
	
	/**
	 * @param pkgDir
	 *            生成的规则包存放目录
	 * @param pkgBackupDir
	 *            生成的规则包备份目录
	 */
	public TopRules(String pkgDir, String pkgBackupDir) {
		this(pkgDir, pkgBackupDir, false);
	}

	@SuppressWarnings("deprecation")
	private String pkgToFile(Package pkg) throws Exception {

		String fileName = pkgDir + File.separatorChar + pkg.getName() + ".pkg";
		Date date = new Date();
		String backUpPath = pkgBackupDir + File.separator;
		String backUpFileName = backUpPath + File.separator + pkg.getName() + "_" + (date.getYear() + 1900) + "_"
				+ (date.getMonth() + 1) + "_" + date.getDate() + "_" + date.getHours() + "_" + date.getMinutes() + "_"
				+ date.getSeconds() + ".pkg";

		File fnewpath = new File(backUpPath);
		if (!fnewpath.exists())
			fnewpath.mkdirs();

		PackageUtil.package2File(pkg, fileName);
//备份规则
//		RuleUtil.copyFile(fileName, backUpFileName);
		return backUpFileName;
	}

	/**
	 * @param in
	 *            规则文件输入流
	 * @return 备份文件名
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public String releaseFromDrl(ByteArrayOutputStream in) throws Exception {
		File tmpFile = File.createTempFile("toprules", "tmp");

		FileOutputStream fin = new FileOutputStream(tmpFile);
		fin.write(in.toByteArray());
		fin.close();
		Package pkg = PackageUtil.file2Package(tmpFile.getAbsolutePath());
		tmpFile.delete();	

		String fileName = pkgToFile(pkg);
		
		SessionProviderFactory sf = new SessionProviderFactory();
		SessionProvider provider = sf.getSessionProvider("toprules_provider");
		//重置规则包
		provider.reset();
		return fileName;
	}

	/**
	 * 发布规则包
	 * 
	 * @param in(XML字节流)
	 * @return 备份文件名
	 */
	@SuppressWarnings("static-access")
	public String releasePkg(ByteArrayOutputStream in){

		try {
			String drl = "";
			String content = new String(in.toByteArray(), "GBK");
			TxnBonusRulePkg pkgRule = PkgXMLConverter.transXMLToPkg(content);
			drl = new CodeGenner().getDRL(pkgRule,isTest);
			
			if(debug){
				System.out.println("drl="+drl);
			}
			Package pkg = PackageUtil.drl2Package(drl);
			
			String fileName =  pkgToFile(pkg);
			
			SessionProviderFactory sf = new SessionProviderFactory();
			SessionProvider provider = sf.getSessionProvider("toprules_provider");
			provider.reset();		
			
			return fileName;
		} catch (DroolsParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行规则包
	 * 
	 * @param objs
	 * @param pkgName
	 *            指定执行哪个pkg
	 */
	@SuppressWarnings("unchecked")
	public void execPkg(List objs, String pkgName) {
		execPkg(objs, pkgName, false);
	}

	/**
	 * 执行规则包
	 * 
	 * @param objs
	 *            业务对象List
	 * @param pkgName
	 *            指定执行哪个pkg
	 * @param debug
	 *            执行结束后是否打印结果
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public void execPkg(List objs, String pkgName, boolean debug) {
		SessionProviderFactory sf = new SessionProviderFactory();
		SessionProvider sp;
		try {
			sp = sf.getSessionProvider("toprules_provider");
			StatefulRuleSession ss = sp.createStatefulRuleSession(pkgName);
			ss.executeRules(objs);
			int size = objs.size();
			int i = 0;
			for (i = 0; i < size; i++) {
				@SuppressWarnings("unused")
				TxnBonus objTmp = (TxnBonus) objs.get(i);
				 //getPkgResults(objTmp);
				if (debug) {
					objTmp.result.dump();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 汇总活动结果
	 * 
	 * @param objTmp
	 */
	// private static void getPkgResults(TxnBonus objTmp) {
	// if(objTmp.result.activityGroupResults!=null){
	// int size = objTmp.result.activityGroupResults.size();
	// int i = 0;
	// for(i=0;i<size;i++){
	// ActivityResult rt = (ActivityResult)
	// objTmp.result.activityGroupResults.get(i);
	// // if(rt.result.isEffect == false || rt.result.isExec==false){
	// // //rt.result = null;
	// // }
	// }
	// }
	// }
	public String getPkgDir() {
		return pkgDir;
	}

	/**
	 * 设置规则包路径
	 * 
	 * @param pkgDir
	 */
	public void setPkgDir(String pkgDir) {
		this.pkgDir = pkgDir;
	}

	public String getPkgBackDir() {
		return pkgBackupDir;
	}

	/**
	 * 规则包备份路径
	 * 
	 * @param pkgBackDir
	 */
	public void setPkgBackDir(String pkgBackupDir) {
		this.pkgBackupDir = pkgBackupDir;
	}

}
