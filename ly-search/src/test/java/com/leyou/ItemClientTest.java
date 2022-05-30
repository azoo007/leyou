package com.leyou;

import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.Sku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class ItemClientTest {

    @Autowired
    private ItemClient itemClient;

    @Test
    public void testfindSkusBySpuId(){
        List<Sku> skus = itemClient.findSkusBySpuId(1L);
        skus.forEach(System.out::println);
    }

}
