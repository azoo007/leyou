package com.leyou.search.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.HighlightUtils;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.pojo.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.dto.SearchResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.SearchRepository;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private ItemClient itemClient;

    /**
     * 数据导入
     */
    public void importData(){
        int page = 1;//当前页码
        int rows = 100;//每页条数
        int totalPage = 1;//总页数

        do {
            //1.分页查询Spu商品数据，直到全部数据查询完毕
            PageResult<SpuDTO> pageResult = itemClient.spuPageQuery(page, rows, null, true);//注意：导入到ES的数据必须是上架的数据

            //2.取出所有商品数据
            List<SpuDTO> spuDTOList = pageResult.getItems();

            //3.判空处理
            if(CollectionUtils.isNotEmpty(spuDTOList)){
                //获取List<Goods>集合
                List<Goods> goodsList = spuDTOList.stream().map( spuDTO -> buildGoods(spuDTO)  ).collect(Collectors.toList());

                //保存List<Goods>数据到ES中
                searchRepository.saveAll(goodsList);
            }


            //获取总页数
            totalPage = pageResult.getTotalPage().intValue();
            page++;
        }while (page<=totalPage);

    }

    /**
     * 将一个SpuDTO对象转换为一个Goods对象
     */
    public Goods buildGoods(SpuDTO spuDTO){
        Goods goods = new Goods();

        goods.setId(spuDTO.getId());
        goods.setSpuName(spuDTO.getName());
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setCreateTime(new Date().getTime());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setBrandId(spuDTO.getBrandId());

        //根据spuId查询所有Sku
        List<Sku> skus = itemClient.findSkusBySpuId(spuDTO.getId());

        //只取出Sku部分属性
        List<Map<String,Object>> skuMapList = new ArrayList<>(); //存储一个Spu的所有Sku数据
        //遍历Sku对象
        if(CollectionUtils.isNotEmpty(skus)){
            skus.forEach(sku -> {
                Map<String,Object> skuMap = new HashMap<>();//封装一个Sku数据
                skuMap.put("id",sku.getId());
                skuMap.put("images",sku.getImages());
                skuMap.put("price",sku.getPrice());

                skuMapList.add(skuMap);
            });
        }
        //将List转换json字符串
        String skusJosn = JsonUtils.toString(skuMapList);

        //获取all属性 (spu的name + spu的subTitle + 所有Sku的title
        String all = spuDTO.getName() +" "+spuDTO.getSubTitle()+" "+skus.stream().map(Sku::getTitle).collect(Collectors.joining(" "));

        //获取price
        Set<Long> price = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());

        goods.setAll(all);
        goods.setSkus(skusJosn);
        goods.setPrice(price);

        /**
         * 格式： Map<String,Object>
         *    key: 规格参数的名称
         *    value: 规格参数的值（包含通用规格参数+特有规格参数）
         *
         *  思路：
         *    1）根据分类（第三级）ID查询用于搜索过滤的规格参数（searching=true）
         *    2) Map的key，就是规格参数名称
         *    3）根据spuId查询SpuDetail数据，取出 generic_spec和special_spec属性值
         *    4）将generic_spec，special_spec转换为Map集合，使用规格参数的ID到Map集合取值
         *    5）根据generic属性判断该参数是否为通用，如果为通用（true），往Map存入第4步取的值，否则，存入第4步取的值
         *
         */
        Map<String,Object> specsMap = new HashMap<>();//用于存储所有搜素过滤的规格参数

        //1）根据分类（第三级）ID查询用于搜索过滤的规格参数（searching=true）
        List<SpecParam> specParams = itemClient.findSpecParams(null, spuDTO.getCid3(), true);

        specParams.forEach(specParam -> {
            //2)Map的key，就是规格参数名称
            String key = specParam.getName();

            Object value = null;

            //3）根据spuId查询SpuDetail数据，取出 generic_spec和special_spec属性值
            SpuDetail spuDetail = itemClient.findSpuDetailBySpuId(spuDTO.getId());
            String genericSpec = spuDetail.getGenericSpec();
            String specialSpec = spuDetail.getSpecialSpec();

            //4）将generic_spec，special_spec转换为Map集合，使用规格参数的ID到Map集合取值
            //转换generic_spec为Map集合
            //toMap():只能转换一层集合架构
            Map<Long, Object> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class, Object.class);
            //转换special_spec为Map集合
            //nativeRead():可以转换多层集合结构
            Map<Long, List<Object>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<Object>>>() {});

            //5）根据generic属性判断该参数是否为通用，如果为通用（true），往Map存入第4步取的值，否则，存入第4步取的值
            if(specParam.getGeneric()){
                //通用规格参数
                value = genericSpecMap.get(specParam.getId());
            }else{
                //特有规格参数
                value = specialSpecMap.get(specParam.getId());
            }

            /**
             * 把数字类型的参数转换为区间范围
             *   思路：
             *     1）判断该参数是否为数字参数（numeric=true），为true才进行第2步
             *     2）取出当前参数的区间（segments），格式：0-4.0,4.0-5.0,5.0-5.5,5.5-6.0,6.0-
             *     3）以逗号切割字符串，获取所有区间，在遍历每个区间，再去使用-去切割每个区间，得到每个区间的起始值和结束值。
             *     4）判断当前参数值，在哪个区间中，如果匹配，则取出当前区间：4.0-5.0
             *     5) 把第4步区间值和参数的单位(unit)拼接，最后成为区间：4.0-5.0英寸 或 4.0英寸以下 或 5.0英寸以上
             */
             if(specParam.getNumeric()){
                 //只有数字类型参数，才去转换区间
                 value = chooseSegment(value,specParam);
             }



            specsMap.put(key,value);
        });


        goods.setSpecs(specsMap);


        return goods;
    }

    /**
     * 将数字型参数转换为区间字符串
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(Object value, SpecParam p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    public SearchResult<GoodsDTO> goodsSearchPage(SearchRequest searchRequest) {
        //1.创建SearchResult<GoodsDTO>
        SearchResult<GoodsDTO> searchResult = new SearchResult<>();

        //2.封装SearchResult<GoodsDTO>
        //2.1 查询商品分页列表数据
        PageResult<GoodsDTO> pageResult = itemQueryPage(searchRequest);

        //2.2 查询搜索过滤条件数据
        Map<String,Object> filterConditions = filterConditionsQuery(searchRequest);

        searchResult.setItems(pageResult.getItems());
        searchResult.setTotal(pageResult.getTotal());
        searchResult.setTotalPage(pageResult.getTotalPage());
        searchResult.setFilterConditions(filterConditions);

        //3.返回SearchResult<GoodsDTO>
        return searchResult;
    }

    /**
     * 查询搜索过滤条件数据
     * @param searchRequest
     * @return
     */
    public Map<String, Object> filterConditionsQuery(SearchRequest searchRequest) {
        //1.创建Map集合  使用有序的Map集合：LinkedHashMap
        Map<String, Object> filterConditionsMap = new LinkedHashMap<>();

        //2.封装Map集合

        //2.1 封装固定过滤条件（分类和品牌）

        //1)获取搜索条件
        NativeSearchQueryBuilder queryBuilder = createNativeQueryBuilder(searchRequest);
        //2）设置结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""},null));
        //3）添加聚合条件
        String categoryAgg = "categoryAgg";
        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));

        //4）执行聚合查询
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(),Goods.class);

        //5）获取所有聚合结果
        Aggregations aggregations = aggregatedPage.getAggregations();

        //6）取出分类的聚合结果
        Terms categoryTerms = aggregations.get(categoryAgg);

        //6.1 取出所有结果的分类ID
        List<Long> categoryIds = categoryTerms.getBuckets()
                                    .stream()
                                    .map(Terms.Bucket::getKeyAsNumber)//以Number类型获取key
                                    .map(Number::longValue) // 使用Number的longValue方法把Number类型转换为Long类型
                                    .collect(Collectors.toList());

        //6.2 把分类的ID集合转换为分类对象集合
        List<Category> categoryList = itemClient.findCategoriesByIds(categoryIds);

        //6.3.把品牌对象集合存入Map集合
        filterConditionsMap.put("分类",categoryList);

        //7）取出品牌的聚合结果
        Terms brandTerms = aggregations.get(brandAgg);

        //7.1 取出所有结果的品牌ID
        List<Long> brandIds = brandTerms.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)//以Number类型获取key
                .map(Number::longValue) // 使用Number的longValue方法把Number类型转换为Long类型
                .collect(Collectors.toList());

        //7.2 把品牌的ID集合转换为品牌对象集合
        List<Brand> brandList = itemClient.findBrandsByIds(brandIds);

        //7.3.把品牌对象集合存入Map集合
        filterConditionsMap.put("品牌",brandList);


        //2.2 封装动态过滤条件（规格参数聚合）

        if(categoryIds!=null){
            categoryIds.forEach(categoryId -> {
                //1）根据分类ID查询参与搜索过滤条件的规格参数
                List<SpecParam> specParams = itemClient.findSpecParams(null, categoryId, true);

                //2）遍历规格参数，逐个添加到聚合条件中
                specParams.forEach(specParam -> {
                    queryBuilder.addAggregation( AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword") );
                });

                //3）执行聚合查询
                AggregatedPage<Goods> specParamAggPage = esTemplate.queryForPage(queryBuilder.build(),Goods.class);

                Aggregations specParamAggs = specParamAggPage.getAggregations();

                //4）遍历规格参数， 获取所有规格参数的聚合结果，把每个规格参数的聚合结果存入Map集合
                specParams.forEach(specParam -> {
                    Terms specParamTerms = specParamAggs.get(specParam.getName());

                    List<Object> specParamAggKeyList = specParamTerms.getBuckets()
                                                        .stream()
                                                        .map(Terms.Bucket::getKey)
                                                        .collect(Collectors.toList());

                    filterConditionsMap.put(specParam.getName(),specParamAggKeyList);
                });
            });
        }


        //3.返回Map集合
        return filterConditionsMap;
    }

    /**
     * 查询商品分页列表数据
     * @param searchRequest
     * @return
     */
    public PageResult<GoodsDTO> itemQueryPage(SearchRequest searchRequest) {
        //1.创建本地查询构造器对象
        NativeSearchQueryBuilder queryBuilder = createNativeQueryBuilder(searchRequest);

        //4) 设置结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","spuName","subTitle","skus"},null));

        //5）设置分页参数
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));

        //6）设置高亮字段与格式
        HighlightUtils.highlightField(queryBuilder,"spuName");

        //3.执行查询，获取结果
        Page<Goods> pageBean = esTemplate.queryForPage(queryBuilder.build(),Goods.class,HighlightUtils.highlightBody(Goods.class,"spuName"));

        //4.处理结果，并返回
        //1）取出Goods集合
        List<Goods> goodsList = pageBean.getContent();
        //2）把Goods集合拷贝到GoodsDTO集合
        List<GoodsDTO> goodsDTOList = BeanHelper.copyWithCollection(goodsList,GoodsDTO.class);
        //3）封装PageResult<GoodsDTO>
        PageResult<GoodsDTO> pageResult = new PageResult<GoodsDTO>(
                pageBean.getTotalElements(),
                Long.valueOf(pageBean.getTotalPages()),
                goodsDTOList
        );

        return pageResult;
    }

    /**
     * 创建查询条件对象
     * @param searchRequest
     * @return
     */
    public NativeSearchQueryBuilder createNativeQueryBuilder(SearchRequest searchRequest) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //2.封装地查询构造器(*)
        //1）创建布尔查询构造器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //2）在布尔查询构造器中追加must条件
        //boolQueryBuilder.must( QueryBuilders.matchQuery("all",searchRequest.getKey()) );
        boolQueryBuilder.must( QueryBuilders.multiMatchQuery(searchRequest.getKey(),"all","spuName") );


        //添加过滤条件
        Map<String, Object> filterParams = searchRequest.getFilterParams();
        if(filterParams!=null){
            //遍历过滤条件
            filterParams.entrySet().forEach(entry->{
                String key = entry.getKey();
                Object value = entry.getValue();

                //处理key
                if(key.equals("分类")){
                    key = "categoryId";
                }else if(key.equals("品牌")){
                    key = "brandId";
                }else{
                    key = "specs."+key+".keyword";
                }

                //把过滤条件存入布尔查找中
                boolQueryBuilder.filter( QueryBuilders.termQuery(key,value)  );
            });
        }


        //3）把布尔查询构造器存入本地查询构造器
        queryBuilder.withQuery(boolQueryBuilder);
        return queryBuilder;
    }

    public List<GoodsDTO> goodsSearchPageChange(SearchRequest searchRequest) {
        PageResult<GoodsDTO> pageResult = itemQueryPage(searchRequest);
        return pageResult.getItems();
    }

    public void createIndex(Long spuId) {
        //根据spuId查询SpuDTO
        SpuDTO spuDTO = itemClient.findSpuDTOBySpuId(spuId);
        Goods goods = buildGoods(spuDTO);
        searchRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        searchRepository.deleteById(spuId);
    }
}
