package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller // 必须是Controller，不能@RestController
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 接收商品详情
     */
    @GetMapping("/item/{id}.html")
    public String showGoodsDetail(@PathVariable("id") Long id, Model model){
        //1.查询数据库，调用业务
        Map<String,Object> resultMap = pageService.getDetailData(id);

        //2.把Map集合数据存入Model
        model.addAllAttributes(resultMap);

        //3.返回th的模板
        return "item";
    }

}
