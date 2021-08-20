package com.xck.jdk.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.SortedMap;

/**
 * AES加密算法测试
 *
 * @author xuchengkun
 * @date 2021/06/23 15:28
 **/
public class AESRSATest {

    public static String publicKeyStr = "";
    public static String privateKeyStr = "";
    public static String aesStr = "";

    public static void main(String[] args) throws Exception{
        generateKeyPair();

        aesStr = encrypt(randomAES(), Base64.decodeBase64(publicKeyStr));
        System.out.println("RSA加密密钥:"+aesStr);

        byte[] aesKey = decrypt(Base64.decodeBase64(aesStr), Base64.decodeBase64(privateKeyStr));
        System.out.println("解密aes:" + new String(aesKey, "UTF-8"));

        String data = "十大平淡是苹果电视柜的排水管非得视频";
        System.out.println("待加密数据:" + data);

        String encryptContent = AESEncrypt(data, aesKey);
        System.out.println("加密数据:" + encryptContent);
        data = AESDecrypt(encryptContent, aesKey);
        System.out.println("解密数据:" + data);

        String signPublicKey = sign(Base64.decodeBase64(publicKeyStr), Base64.decodeBase64(privateKeyStr));
        System.out.println("签名公钥:" + signPublicKey);

        boolean checkResult = checkSign(Base64.decodeBase64(publicKeyStr), Base64.decodeBase64(signPublicKey)
                , Base64.decodeBase64(publicKeyStr));
        System.out.println("签名验证: " + checkResult);
    }

    /**
     * 生成RSA密钥对，拿到密钥对的base64字符串
     * @throws Exception
     */
    public static void generateKeyPair() throws Exception{
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        System.out.println("公钥:"+(publicKeyStr = Base64.encodeBase64String(publicKeyBytes)));

        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        System.out.println("私钥"+(privateKeyStr = Base64.encodeBase64String(privateKeyBytes)));
    }

    /**
     * 随机生成16位AES密钥，同理，拿到base64字符串
     * @throws Exception
     */
    public static byte[] randomAES() throws Exception{
        return RandomStringUtils.randomAlphanumeric(16).getBytes("UTF-8");
    }

    public static String AESEncrypt(String data, byte[] key) throws Exception{
        SecretKeySpec skey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skey);
        return Base64.encodeBase64String(cipher.doFinal(data.getBytes("UTF-8")));
    }

    public static String AESDecrypt(String data, byte[] key) throws Exception{
        byte[] dataBytes = Base64.decodeBase64(data);
        SecretKeySpec skey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skey);
        return new String(cipher.doFinal(dataBytes), Charset.forName("UTF-8"));
    }

    /**
     * AES加密字符串
     *
     * @param dataBytes 需要被加密的字符串
     * @param pwd 加密需要的密码
     * @return 密文
     */
    public static String encrypt(byte[] dataBytes, byte[] pwd) throws Exception{
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pwd);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey)keyFactory.generatePublic(x509KeySpec);
        Cipher publicCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        publicCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int inputLen = dataBytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > 117) {
                cache = publicCipher.doFinal(dataBytes, offSet, 117);
            } else {
                cache = publicCipher.doFinal(dataBytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * 117;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        return Base64.encodeBase64String(encryptedData);
    }

    /**
     * 解密AES加密过的字符串
     *
     * @param data
     *            AES加密过过的内容
     * @param pwd
     *            加密时的密码
     * @return 明文
     */
    public static byte[] decrypt(byte[] data, byte[] pwd) throws Exception{

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(pwd);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey publicKey = (RSAPrivateKey)keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher publicCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        publicCipher.init(Cipher.DECRYPT_MODE, publicKey);

        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > 128) {
                cache = publicCipher.doFinal(data, offSet, 128);
            } else {
                cache = publicCipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * 128;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        return encryptedData;
    }

    public static String sign(byte[] data, byte[] pwd) throws Exception{
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(pwd);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return Base64.encodeBase64String(signature.sign());
    }

    public static boolean checkSign(byte[] src, byte[] sign, byte[] pwd) throws Exception{
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pwd);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey)keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initVerify(publicKey);
        signature.update(src);
        return signature.verify(sign);
    }
}
