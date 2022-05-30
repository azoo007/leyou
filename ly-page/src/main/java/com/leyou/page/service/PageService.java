package com.leyou.page.service;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
    @Autowired
    private ItemClient itemClient;

    @Value("${ly.static.itemDir}")
    private String itemDir;
    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public Map<String,Object> getDetailData(Long id) {

        //1.查询SpuDTO对象
        SpuDTO spuDTO = itemClient.findSpuDTOBySpuId(id);
        //2.根据分类ID集合查询分类对象集合
        List<Category> categories = itemClient.findCategoriesByIds(Arrays.asList(
                spuDTO.getCid1(),
                spuDTO.getCid2(),
                spuDTO.getCid3()));
        //3.根据品牌ID查询品牌
        Brand brand = itemClient.findBrandById(spuDTO.getBrandId());
        //4.根据分类ID查询规格组（包含组内参数）
        List<SpecGroupDTO> specGroupDTOList = itemClient.findSpecGroupDTOByCid(spuDTO.getCid3());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("categories",categories);
        resultMap.put("brand",brand);
        resultMap.put("spuName",spuDTO.getName());
        resultMap.put("subTitle",spuDTO.getSubTitle());
        resultMap.put("detail",spuDTO.getSpuDetail());
        resultMap.put("skus",spuDTO.getSkus());
        resultMap.put("specs",specGroupDTOList);
        return resultMap;
    }

    /**
     * 生成商品的静态页面
     */
    public void createStaticPage(Long spuId){
        //1）创建Context上下文对象（读取模板文件需要的动态数据）
        Context context = new Context();
        //设置动态数据
        context.setVariables(getDetailData(spuId));

        //2）定义静态模板（(item.html）
        String tmepName = itemTemplate+".html";

        //3）使用模板引擎对象读取静态目标，把动态数据写入模板，使用IO输出流生成一个静态文件

        //生成 静态页名称
        String fileName = spuId+".html";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File(itemDir,fileName));
            templateEngine.process(tmepName,context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            //注意：一旦涉及自定义输出流，必须关闭输出流，否则后续无法删除该文件
            writer.close();
        }
    }

    public void deleteStaticPage(Long spuId) {
        //1.读取文件
        File file = new File(itemDir , spuId+".html" );
        //2.删除文件
        if(file.exists()){
            file.delete();
        }
    }
}
