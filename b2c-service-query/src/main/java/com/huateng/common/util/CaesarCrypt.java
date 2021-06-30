package com.huateng.common.util;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Random;

/**
 * 会员二维码参数加密类
 * 
 */
public class CaesarCrypt {

	private String table;
	
	public CaesarCrypt(String table){
		this.table = table;
	}
	
	public String encrypt(String text, int shiftNum) {
		int len = table.length();

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			char curChar = text.charAt(i);
			int idx = table.indexOf(curChar);
			if (idx == -1) {
				result.append(curChar);
				continue;
			}
			idx = ((idx + shiftNum) % len);
			result.append(table.charAt(idx));
		}

		return result.toString();
	}
	
	
	
	public String decrypt(String text, int shiftNum){
		shiftNum = table.length() + (shiftNum % table.length());

		return encrypt(text, shiftNum);
	}
	
	public static int randInt(int min, int max) {

	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	//88208000431
	public void dynamicQRCode(String cardID){
		//随机秘钥
		int key = 201607217;
		//随机数
		int random = CaesarCrypt.randInt(10000000,99999999);
		//时间戳	
		String timestampStr = new Long(new Date().getTime()/1000).toString();
		
		//补位后的卡号和时间戳
		String cardID0 = StringUtils.leftPad(cardID, 19, '0');
		String timestampStr0 = StringUtils.leftPad(timestampStr, 12, '0');
		System.out.println("cardID0:" + cardID0);
		System.out.println("timestampStr0:" + timestampStr0);
		
		
		//2.利用随机数对卡号及时间戳加密
		String encodeCarid = encrypt(cardID0, random);
		String encodeTimestamp = encrypt(timestampStr0, random);
		//3.利用对称秘钥对随机数加密
		String encodeRandom = encrypt(String.valueOf(random), key);
		//4.拼装加密后的密文
		System.out.println("encodeCarid:" + encodeCarid);
		System.out.println("encodeTimestamp:" + encodeTimestamp);
		String encodeResult = encodeCarid+encodeTimestamp+encodeRandom;
		System.out.println("密文："+encodeResult);
		
		/*~~~~~~~~~~~解密~~~~~~~~~~~*/
		//分割字符串
		String encodeCarid1 = StringUtils.substring("666665555544444555577813634275735376414", 0, 19);
		String encodeTimestamp1 = StringUtils.substring("666665555544444555577813634275735376414", 19, 31);
		String encodeRandom1 = StringUtils.substring("666665555544444555577813634275735376414", 31, 40);
		
		//1、解密随机数
		int decodeRandom = new Integer(decrypt(encodeRandom1, -key));

		//2.利用随机数解开卡号，时间戳
		String decodeCarid = decrypt(encodeCarid1, -decodeRandom).replaceFirst("^0+", "");
		String decodeTimestamp = decrypt(encodeTimestamp1, -decodeRandom).replaceFirst("^0+", "");
		
		//3.验证结果，如果验证不通过则报错
		if(!cardID.equals(decodeCarid)){
			//throw new RuntimeException("验证结果不正确！");
			System.out.println("#####################失败");
			System.out.println("虚拟卡号：" + decodeCarid);
			System.out.println("时间戳：" + decodeTimestamp);
		}else {
			System.out.println("~~~~~~~~~~~~~~~~~~~~成功");
			System.out.println("卡号：" + decodeCarid);
			System.out.println("时间戳：" + decodeTimestamp);
		}
	}
	
	
	
//	public static void main(String[] args) throws Exception{
//		String cardId = "9999988888777778888";
//		CaesarCrypt caesar = new CaesarCrypt("0123456789");
//		caesar.dynamicQRCode(cardId);
//
//		/*int threadSize = 20;//并发
//		final int requestSize = 100;//100用户
//		ExecutorService executor = Executors.newFixedThreadPool(threadSize);
//		while(threadSize > 0){
//
//			executor.submit( new Callable<String>(){
//
//				@Override
//				public String call() throws Exception {
//
//					for(int j=0;j<requestSize;j++){
//						//System.out.println(j);
//						CaesarCrypt caesar = new CaesarCrypt("0123456789");//序列表
//						caesar.dynamicQRCode("8820800044");//会员卡号
//					}
//
//					return null;
//				}
//
//			});
//			threadSize--;
//		}
//		executor.shutdown();*/
//	}
}
