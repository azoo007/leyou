package com.ithiema;

import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class RsaTest {

    //公钥路径
    public final String publicKeyPath = "D:\\leyou_projects\\javaee148\\software\\rsa-key\\rsa-key.pub";
    //私钥路径
    public final String privateKeyPath = "D:\\leyou_projects\\javaee148\\software\\rsa-key\\rsa-key";

    /**
     * 生成公私钥
     * @throws Exception
     */
    @Test
    public void testGenerteKey() throws Exception {
        RsaUtils.generateKey(publicKeyPath,privateKeyPath,"itheima",2048);
    }

    //读取公钥
    @Test
    public void testGetPublicKey() throws Exception {
        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);
        System.out.println(publicKey);
    }

    //读取私钥
    @Test
    public void testGetPrivateKey() throws Exception {
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        System.out.println(privateKey);
    }
}
