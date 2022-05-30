package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.pojo.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品业务
 */
@Service
@Transactional
public class GoodsService extends ServiceImpl<SkuMapper,Sku> {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuDTO> spuPageQuery(Integer page, Integer rows, String key, Boolean saleable) {
        //1.封装条件
        //1.1 封装分页参数
        IPage<Spu> iPage = new Page<>(page,rows);

        //1.2 封装查询条件
        QueryWrapper<Spu> queryWrapper = Wrappers.query();

        //拼接条件
        //SELECT * FROM tb_spu WHERE (NAME LIKE '%华为%' OR sub_title LIKE '%华为%') AND saleable=1
        //处理key
        if(StringUtils.isNotEmpty(key)){
            // where name like '%xx%' or sub_title like '%xx%'

            //and:该方法可以把一些拼接的条件（sql语句）作为一个整体提高sql执行优先级（给一些条件加括号）
            queryWrapper.and(
                    i->
                    i.like("name",key)
                     .or()
                     .like("sub_title",key)
            );


        }

        //处理saleable
        if(saleable!=null){
            // saleable=true/false
            queryWrapper.eq("saleable",saleable);
        }

        //2.查询数据，获取结果
        iPage = spuMapper.selectPage(iPage,queryWrapper);

        //3.处理并返回结果
        //3.1 取出所有Spu对象
        List<Spu> spuList = iPage.getRecords();
        //3.2 拷贝数据，从Spu对象拷贝到SpuDTO对象
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(spuList,SpuDTO.class);
        //3.3 封装分类名称和品牌名称
        getCategoryNameAndBrandName(spuDTOList);
        //3.4 封装PageResult
        PageResult<SpuDTO> pageResult = new PageResult<>(iPage.getTotal(),iPage.getPages(),spuDTOList);
        return pageResult;
    }

    /**
     * 在spuDTOList集合中添加分类名称和品牌名称两个属性
     * @param spuDTOList
     */
    public void getCategoryNameAndBrandName(List<SpuDTO> spuDTOList) {
        spuDTOList.forEach(spuDTO -> {
            //1.处理品牌
            Brand brand = brandService.findBrandById(spuDTO.getBrandId());
            spuDTO.setBrandName(brand.getName());

            //2.处理分类
            //2.1 根据分类ID集合查询分类对象集合
            List<Category> categoryList = categoryService.findCategoriesByIds(Arrays.asList(spuDTO.getCid1(), spuDTO.getCid2(), spuDTO.getCid3()));
            //2.2 格式： 手机通讯/手机/手机
            String categoryName = categoryList.stream().map(Category::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryName);
        });
    }

    public void saveGoods(SpuDTO spuDTO) {
        //1.保存spu表
        //数据拷贝
        try {
            Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
            //商品默认下架
            spu.setSaleable(false);
            spuMapper.insert(spu);

            //2.保存spuDetail表
            SpuDetail spuDetail = spuDTO.getSpuDetail();
            //必须记得设置spu的ID
            spuDetail.setSpuId(spu.getId());
            spuDetailMapper.insert(spuDetail);

            //3.保存sku表
            List<Sku> skus = spuDTO.getSkus();

            skus.forEach(sku -> {
                //必须记得设置spu的ID
                sku.setSpuId(spu.getId());
                //保存sku
                //skuMapper.insert(sku);
            });

            //调用MyBatis-Plus的批量保存
            saveBatch(skus);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    public void updateSaleable(Long id, Boolean saleable) {
        try {
            Spu spu = new Spu();
            spu.setId(id);//必须设置数据库存在ID
            spu.setSaleable(saleable);

            //修改状态
            spuMapper.updateById(spu);  // updateById: MyBatis-Plus底层自动判断非NULL值才更新

            //根据上下架业务发生消息给MQ
            String routingKey = saleable?MQConstants.RoutingKey.ITEM_UP_KEY:MQConstants.RoutingKey.ITEM_DOWN_KEY;
            amqpTemplate.convertAndSend(MQConstants.Exchange.ITEM_EXCHANGE_NAME,routingKey,id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public List<Sku> findSkusBySpuId(Long id) {
        //1.封装条件
        Sku sku = new Sku();
        sku.setSpuId(id);
        QueryWrapper<Sku> queryWrapper = Wrappers.query(sku);

        //2.执行查询，获取结果
        List<Sku> skus = skuMapper.selectList(queryWrapper);

        //3.处理结果并返回
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return skus;

    }

    public SpuDetail findSpuDetailBySpuId(Long id) {
        //spu和spu_detail 共享主键
        SpuDetail spuDetail = spuDetailMapper.selectById(id);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;
    }

    public SpuDTO findSpuDTOBySpuId(Long id) {
        //1.根据spuId查询Spu对象
        Spu spu = spuMapper.selectById(id);
        //2.拷贝数据
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        //3.查询SpuDetail
        SpuDetail spuDetail = findSpuDetailBySpuId(id);
        spuDTO.setSpuDetail(spuDetail);
        //4.查询所有Sku对象
        List<Sku> skus = findSkusBySpuId(id);
        spuDTO.setSkus(skus);
        return spuDTO;
    }

    public List<Sku> findSkusByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectBatchIds(ids);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return skus;
    }

    public void minusStock(Map<Long, Integer> paramMap) {
        paramMap.entrySet().forEach(entry->{
            Long skuId = entry.getKey();

            Integer num = entry.getValue();

            //查询现有商品
            Sku sku = skuMapper.selectById(skuId);

            //判断库存量是否足够
            if(sku.getStock()>=num){
                //扣减库存
                sku.setStock(sku.getStock()-num);
                //刷新到数据库
                skuMapper.updateById(sku);
            }
        });

    }
}
