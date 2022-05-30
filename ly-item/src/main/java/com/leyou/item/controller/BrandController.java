package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 品牌
 */
@RestController
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 品牌分页查询
     */
    @GetMapping("/brand/page")
    public ResponseEntity<PageResult<Brand>> brandPageQuery(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",required = false) Boolean desc
    ){
        PageResult<Brand> pageResult = brandService.brandPageQuery(page,rows,key,sortBy,desc);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增品牌
     * 1) 使用对象接收的情况
     *  Brand brand: 用于接收页面的普通参数，例如：name=xxx&letter=C&image=xxx
     *  @RequestBody Brand brand: 用于接收页面的json参数，例如：{name:"xxx",letter:"C",image:"xxx"}
     *
     * 2）接收页面的同名参数
     *    页面上有两种情况传递的同名参数
     *     1）复选框提交的格式 例如   ids=1&ids=2&ids=3....
     *     2）使用逗号拼接格式 例如   ids=1,2,3....
     *    后台如何接收同名参数
     *       1）字符串     String ids    内容：1,2,3....
     *       2) 数组    Long[] ids     内容：[1,2,3]
     *       3）List集合  List<Long> ids  内容：[1,2,3]  注意：List集合必须添加@RequestParam注解
     */
    @PostMapping("/brand")
    public ResponseEntity<Void> saveBrand(
            Brand brand,
            @RequestParam("cids") List<Long> cids
    ){
        brandService.saveBrand(brand,cids);
        //return ResponseEntity.status(HttpStatus.CREATED).body(null);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/brand/{id}")
    public ResponseEntity<Brand> findBrandById(@PathVariable("id") Long id){  //@PathVariable:用于接收路径的参数
        Brand brand = brandService.findBrandById(id);
        return ResponseEntity.ok(brand);
    }

    /**
     * 根据分类ID查询品牌列表
     */
    @GetMapping("/brand/of/category")
    public ResponseEntity<List<Brand>> findBrandsByCid(@RequestParam("id") Long id){
        List<Brand> brandList = brandService.findBrandsByCid(id);
        return ResponseEntity.ok(brandList);
    }

    /**
     * 根据品牌ID集合查询品牌对象集合
     */
    @GetMapping("/brand/list")
    public ResponseEntity<List<Brand>> findBrandsByIds(@RequestParam("ids") List<Long> ids){
        List<Brand> brands = brandService.findBrandsByIds(ids);
        return ResponseEntity.ok(brands);
    }
}
