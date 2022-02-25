package com.xsdhy.captcha.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * @author geo
 */
@Slf4j
public class SecureUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * AES加密
     *
     * @param passwd  加密的密钥
     * @param content 需要加密的字符串
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String passwd, String content) {
        if (Strings.isBlank(content) || Strings.isBlank(passwd)) {
            return "";
        }
        // 创建密码器
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            // 初始化为加密模式的密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(passwd));
            // 加密
            byte[] result = cipher.doFinal(byteContent);
            //通过Base64转码返回
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            log.error(content + "数据加密失败");
            return null;
        }
    }

    /**
     * AES解密
     *
     * @param passwd    加密的密钥
     * @param encrypted 已加密的密文
     * @return 返回解密后的数据
     */
    public static String decrypt(String passwd, String encrypted) {
        if (Strings.isBlank(encrypted) || Strings.isBlank(passwd)) {
            return "";
        }
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(passwd));
            //执行操作
            byte[] result = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(encrypted + "数据解密失败");
            return null;
        }
    }

    /**
     * 生成加密秘钥
     *
     * @return
     */
    private static SecretKeySpec getSecretKey(final String password) throws NoSuchAlgorithmException {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password.getBytes());
        //AES 要求密钥长度为 128
        kg.init(128, random);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        // 转换为AES专用密钥
        return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    }
}
