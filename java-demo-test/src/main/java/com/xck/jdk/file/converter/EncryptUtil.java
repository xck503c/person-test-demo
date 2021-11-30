package com.xck.jdk.file.converter;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.digest.MD5;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密工具
 *
 * @author xuchengkun
 * @date 2021/10/25 17:27
 **/
public class EncryptUtil {

    /**
     * 加密
     * @param text
     * @param key
     * @return
     * @throws Exception
     */
    public static String encodeAES(String text, String key) throws Exception {
        byte[] keybBytes = MD5.create().digest(key);
        byte[] passwdBytes = text.getBytes();
        byte[] aesBytyes = encrypt(passwdBytes, keybBytes);
        return new String(Base64.encode(aesBytyes));
    }

    public static String deCodeAES(String password, String key) throws Exception {
        byte[] keybBytes = MD5.create().digest(key);
        byte[] debase64Bytes = Base64.decode(password.getBytes());
        return new String(decrypt(debase64Bytes, keybBytes));
    }

    public static byte[] encrypt(byte[] text, byte[] key) throws Exception {
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(text);
    }

    public static byte[] decrypt(byte[] text, byte[] key) throws Exception {
        SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(text);
    }
}
