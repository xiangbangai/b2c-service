package com.huateng.common.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created with cardweb.
 * User: Sam
 * Date: 2018/11/20
 * Time: 16:46
 * Description:
 */
@Slf4j
public class SecurityUtil {

    private SecurityUtil() {
    }

    private static final String CERT_TYPE = "RSA";

    private static final String SHA512WITHRSA = "SHA512withRSA";

    private static final String ALGORITHM_SHA1 = "SHA-1";

    private static final String CHARSET_UTF8 = "UTF-8";

    private static PrivateKey privateKey = null;

    private static final int MAX_ENCRYPT_BLOCK = 245;
    private static final int MAX_DECRYPT_BLOCK = 256;

    public static String InputStreamToStr(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(CHARSET_UTF8).replaceAll("\\n|\\t","");
    }

    public static byte[] sha1X16(String data){
        try {
            byte[] bytes = sha1(data.getBytes(CHARSET_UTF8));
            StringBuilder sha1StrBuff = new StringBuilder();
            for (int i = 0; i < bytes.length; ++i) {
                if (Integer.toHexString(0xFF & bytes[i]).length() == 1)
                    sha1StrBuff.append("0").append(Integer.toHexString(0xFF & bytes[i]));
                else
                    sha1StrBuff.append(Integer.toHexString(0xFF & bytes[i]));
            }
            return sha1StrBuff.toString().getBytes(CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static byte[] sha1(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(ALGORITHM_SHA1);
            md.reset();
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static PrivateKey getPrivateKey(String key) {
        if(privateKey == null) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(CERT_TYPE);
                PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
                privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
                return privateKey;
            } catch (Exception e) {
                log.error("加载证书失败：" + e.getMessage());
            }
        }
        return privateKey;
    }

    public static PublicKey getPublicKey(String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(CERT_TYPE);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (Exception e) {
            log.error("读取公钥失败：" + e.getMessage(),e);
        }
        return null;
    }

    public static String getSign(byte[] signDigest,String privateKey) {
        try {
            Signature signature = Signature.getInstance(SHA512WITHRSA);
            signature.initSign(SecurityUtil.getPrivateKey(privateKey));
            signature.update(signDigest);
            byte[] signed = signature.sign();
            return Base64.encodeBase64String(signed);
        } catch (Exception e) {
            log.error("签名失败：" + e.getMessage(),e);
        }
        return null;
    }


    public static boolean verifySign(String signSrc, String signStr, PublicKey publicKey) {
        try {
            byte[] signDigest = SecurityUtil.sha1X16(signSrc);
            Signature signature = Signature.getInstance(SHA512WITHRSA);
            signature.initVerify(publicKey);
            signature.update(signDigest);
            return signature.verify(Base64.decodeBase64(signStr));
        } catch (Exception e) {
            log.warn("验签失败：" + e.getMessage(),e);
        }
        return false;
    }

    public static String encrypt(PublicKey publicKey, String textData) {
        Cipher cipher = null;
        byte[] data = null;
        byte[] encryptedData = new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            data = textData.getBytes(CHARSET_UTF8);
            cipher = Cipher.getInstance(CERT_TYPE);//java默认RSA/ECB/PKCS1Padding
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int inputLength = data.length;
            byteArrayOutputStream = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            while (inputLength - offset > 0) {
                if (inputLength - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offset, inputLength - offset);
                }
                byteArrayOutputStream.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK;
            }
            encryptedData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (Exception e) {
            log.warn("加密失败：" + e.getMessage());
        }
        return Base64.encodeBase64String(encryptedData);
    }


    public static String decryptByPrivateKey(String textDataSrc, String privateKey){
        Cipher cipher = null;
        byte[] data = null;
        byte[] decryptedData = new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            data = Base64.decodeBase64(textDataSrc);
            cipher = Cipher.getInstance(CERT_TYPE);//java默认RSA/ECB/PKCS1Padding
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
            int inputLength = data.length;
            byteArrayOutputStream = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            byte[] tmp;
            while (inputLength - offset > 0) {
                if (inputLength - offset > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offset, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offset, inputLength - offset);
                }
                byteArrayOutputStream.write(cache);
                i++;
                offset = i * MAX_DECRYPT_BLOCK;
            }
            decryptedData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        } catch (Exception e) {
            log.warn("解密失败：" + e.getMessage());
        }
        return new String(decryptedData);
    }
}
