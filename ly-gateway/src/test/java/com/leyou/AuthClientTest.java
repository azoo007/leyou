package com.leyou;

import com.leyou.auth.client.AuthClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LyGatewayApplication.class)
public class AuthClientTest {

    @Autowired
    private AuthClient authClient;

    @Test
    public void testauthorization(){
        String token = authClient.authorization("api-gateway", "api-gateway");
        System.out.println("服务token="+token);
    }

}
