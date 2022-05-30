package com.leyou.item.dto;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Spu的数据传输对象
 */
@Data
public class SpuDTO{
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String name;// 商品名称
    private String subTitle;// 子标题
    private Boolean saleable;// 是否上架
    private Date createTime;// 创建时间
    private Date updateTime;// 最后修改时间


    private String categoryName;//封装三个级别分类的名称，格式：手机通讯/手机/手机

    private String brandName;//封装品牌名称 ，格式：华为

    private List<Sku> skus; // 所有Sku对象
    private SpuDetail spuDetail;//Spu明细对象

}
