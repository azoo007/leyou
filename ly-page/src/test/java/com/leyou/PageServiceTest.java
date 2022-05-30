package com.leyou;

import com.leyou.page.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LyPageApplication.class)
public class PageServiceTest {

    @Autowired
    private PageService pageService;

    @Test
    public void testCreateStaticPage(){
        pageService.createStaticPage(88L);
    }


}
