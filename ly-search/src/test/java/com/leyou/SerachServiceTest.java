package com.leyou;

import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试导入数据
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class SerachServiceTest
{
    @Autowired
    private SearchService searchService;

    @Test
    public void testImportData(){
        searchService.importData();
    }
}
