package com.huateng.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;


/**
 * 用于REST OPEN API的请求签名辅助类.
 *
 * @author Marco.hu(huzhiguo@cvte.cn)
 *
 */
public class HttpRequestSiningHelper {

    private HttpRequestSiningHelper() {
    }

    private static String DEFAULT_METHOD = "HmacMD5";//默认算法
	
	final static int fiveMinutes = 60 * 5000;//客户端请求超时时间差
	
	
	
	public static String createSignatureBase(final String method,final String dateHeader, final String requestUri, final String body) {
		final StringBuffer builder = new StringBuffer();

		builder.append(method).append("\n");
		builder.append(dateHeader).append("\n");
		builder.append(requestUri).append("\n");
		try {
			if(body!=null && !"".equals(body.trim())) builder.append(EncryptUtils.MD5Encode(body).trim());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	/**
	 * 创建请求签名.
	 * @param method
	 * @param dateHeader
	 * @param requestUri
	 * @param body
	 * @param secretKey
	 * @return
	 */
	public static String createRequestSignature(final String method,final String dateHeader, final String requestUri,final String body, final String secretKey) {
		try {	
			final SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(),DEFAULT_METHOD);
			final Mac mac = Mac.getInstance(DEFAULT_METHOD);
			mac.init(keySpec);
			final String signatureBase = createSignatureBase(method,dateHeader, requestUri, body);
			final byte[] result = mac.doFinal(signatureBase.getBytes());
			return encodeBase64WithoutLinefeed(result);

		} catch (final Exception e) {
			throw new RuntimeException("不可能的异常.", e);
		}
	}
	

	
	/**
	 * Base64加密.
	 * @param result
	 * @return
	 */
	protected static String encodeBase64WithoutLinefeed(byte[] result) {
		return EncryptUtils.base64Encode(result).trim();
	}

	
	 private static Calendar cal(int field,int amount){
		final Calendar c = Calendar.getInstance(); 
		c.add(field, amount);
		return c;
	}

}
