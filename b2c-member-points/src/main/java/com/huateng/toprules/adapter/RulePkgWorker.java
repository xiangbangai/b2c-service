package com.huateng.toprules.adapter;

import com.huateng.common.util.DateUtil;
import com.huateng.config.rule.RuleConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 
 *  <p><strong>Description:</strong>规则工具类  </p>
 * @author ZYK
 * @Company 上海华腾软件系统有限公司
 * @version 1.0
 * @since JDK1.5
 *
 * Copyright 2014, Shanghai Huateng Software Systems Co., Ltd.
 * All right reserved.
 *
 */
@Slf4j
public class RulePkgWorker {

	private TopRules topRules = null;
	private String updateyyyyMMdd="";
	private RuleConfig ruleConfig;

	public RulePkgWorker(RuleConfig ruleConfig) {
		this.ruleConfig = ruleConfig;
	}

	/**
	 *
	 *<p><strong>Description:</strong>规则文件加载到内存中，直接调用   </p>
	 * @return
	 * @throws Exception
	 * @author ZYK
	 * @update 日期: 2015-5-7
	 */
	public TopRules getRuleFile() throws Exception {
		String curyyyyMMdd = DateUtil.getCurrentDateyyyyMMdd();
		//当规则为空时或者规则文件更新时间已经超过一小时重新加载规则文件
		if (topRules == null||!curyyyyMMdd.equals(updateyyyyMMdd)) {
			log.info("重新加载规则文件");
			topRules = genRuleFile();
			updateyyyyMMdd = curyyyyMMdd;
		}

		return topRules;
	}

	/**
	 *
	 * <p>
	 * <strong>Description:</strong> 加载规则文件
	 * </p>
	 *
	 * @return
	 * @throws Exception
	 * @author <a href="mailto: zhan_yaokang@huateng.com">zhanyaokang</a>
	 * @update 日期: 2012-9-25
	 */
	public TopRules genRuleFile() throws Exception {
		String loca = ruleConfig.getLocale();
		String fileName = ruleConfig.getFile();
		String ruleFullName = loca + fileName;
		File file = new File(ruleFullName);
		// logger.info("file.isFile()：" + file.isFile());
		if (!file.isFile()) {
			log.info("找不到规则文件：" + ruleFullName);
			throw new Exception("找不到规则文件");
		}
		log.info("加载规则文件：" + ruleFullName);
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			byte[] bytes = new byte[4096];
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			int len = in.read(bytes);
			while (len != -1) {
				stream.write(bytes, 0, len);
				len = in.read(bytes);
			}
			// 检查是否存在目录 (Check whether there are directories)
			TopRules rules = new TopRules();
//			String backUpfileName = rules.releaseFromDrl(stream);
			return rules;

		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}


	/**
	 *
	 *<p><strong>Description:</strong> 清理规则文件缓存  </p>
	 * @author ZYK
	 * @update 日期: 2015-7-14
	 */
	public void clearRuleFileCache(){
		topRules = null;
		log.info("清理topRules完成");
	}

}
