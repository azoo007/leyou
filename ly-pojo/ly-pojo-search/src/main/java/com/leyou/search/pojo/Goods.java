package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.Set;

/**
 * 商品索引库实体类
 *  一个Goods对象对应一个Spu对象
 */
@Data
@Document(indexName = "goods",type = "docs")
public class Goods {
    @Id
    private Long id; //spuId

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String spuName;//商品名称，为了能够高亮显示，spuName也进行索引，分词。

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String all;//把商品用于搜索数据拼接成一个整体（使用空格拼接）     索引，分词

    @Field(type = FieldType.Keyword,index = false)
    private String subTitle;//副标题    不索引，不分词

    @Field(type = FieldType.Keyword,index = false)
    private String skus;//商品的所有Sku数据  注意：对象（集合）类型里面对象属性所有都是默认索引和分词的   suks的内容:List集合的json字符串(好处：改变属性为不索引，不分词)

    @Field(type = FieldType.Long )
    private Set<Long> price;  // 存储所有Sku的价格，  索引，不分词

    @Field(type = FieldType.Long )
    private Long categoryId;//商品分类ID    索引，不分词

    @Field(type = FieldType.Long )
    private Long brandId;//商品品牌ID    索引，不分词

    @Field(type = FieldType.Object )
    private Map<String,Object> specs;//商品的所有用于搜索过滤的规格参数   索引，分词

    @Field(type = FieldType.Long )
    private Long createTime;//创建时间   索引，不分词
}
