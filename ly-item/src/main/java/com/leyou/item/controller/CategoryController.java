package com.leyou.item.controller;

import com.leyou.common.constants.LyConstants;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
//@CrossOrigin // 跨域配置，返回特定相同给浏览器
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询分类
     */
    @GetMapping("/category/of/parent")
    public ResponseEntity<List<Category>> findCategoriesByPid(@RequestParam("pid") Long pid){
        List<Category> categories = categoryService.findCategoriesByPid(pid);
        //return ResponseEntity.status(HttpStatus.OK).body(categories);
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据分类ID集合查询分类对象集合
     */
    @GetMapping("/category/list")
    public ResponseEntity<List<Category>> findCategoriesByIds(@RequestParam("ids") List<Long> ids){
        List<Category> categories = categoryService.findCategoriesByIds(ids);
        return ResponseEntity.ok(categories);
    }
}
