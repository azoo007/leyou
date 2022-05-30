package com.ithiema;

import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.Payload;
import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.auth.utils.UserInfo;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    //公钥路径
    public final String publicKeyPath = "D:\\leyou_projects\\javaee148\\software\\rsa-key\\rsa-key.pub";
    //私钥路径
    public final String privateKeyPath = "D:\\leyou_projects\\javaee148\\software\\rsa-key\\rsa-key";


    /**
     * 生成token
     * @throws Exception
     */
    @Test
    public void testGenerateToken() throws Exception {
        UserInfo userInfo = new UserInfo(1L,"jack","admin");
        //获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        String token = JwtUtils.generateTokenExpireInSeconds(userInfo, privateKey, 40);
        System.out.println(token);
    }

    /**
     * 解析token
     */
    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxLFwidXNlcm5hbWVcIjpcImphY2tcIixcInJvbGVcIjpcImFkbWluXCJ9IiwianRpIjoiWTJSa1pERXhaakV0TURrek55MDBOMlEyTFdFNU1XSXRPV1V5TnpjeE5HRXlOVGcyIiwiZXhwIjoxNjE1OTUxMzI5fQ.ZEVhrGXFD_pn_YRJkk3Dpg3fZRgFkTxmhF3IeC6CxK5XMbdgjZefXI5bbUOdIgT7khyHCQyV7y7mLHv-SDvVqiEBHSt-rfQicsL5AL3T2IPwYobJQ5whMdHzetZIotvUsibGGBxGt1c1eilLRmyxV7QT01SvFv82jEiOoX8K8QnKLAdSYCgOqF7Hp4X7Ds8vjbVFhSVEyhXpR7qDeZclPTGVc35Uq1TbX4nkDlJHGmWsD-DETsknSzF55E6OjrhOOLc9v5q-HXNzcxM8Zu0CSPTLQ9j0Go4hOUMag0ocSNu3Rmp3MhKjEKDd7edotuE6gCbeOC3i65l98zzXF27ODg";
        //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);

        Payload<UserInfo> payload = null;

        try {
            payload = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);

            //取出登录用户信息
            UserInfo userInfo = payload.getInfo();

            System.out.println("合法用户，内容为："+userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("未登录，不合法用户");
        }
    }
}
