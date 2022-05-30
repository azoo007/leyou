package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> findSpecGroupsByCids(Long id) {
        //1.封装条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(id);
        QueryWrapper<SpecGroup> queryWrapper = Wrappers.query(specGroup);

        //2.执行查询，获取数据
        List<SpecGroup> specGroups = specGroupMapper.selectList(queryWrapper);

        //3.处理并返回
        if(CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return specGroups;
    }

    public List<SpecParam> findSpecParams(Long gid, Long cid, Boolean searching) {

        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid); // 注意：MyBatis-Plus自动判断对象的属性值是否为NULL，如果为NULL，不拼接该条件
        specParam.setCid(cid);
        specParam.setSearching(searching);

        QueryWrapper<SpecParam> queryWrapper = Wrappers.query(specParam);

        //2.执行查询，获取数据
        List<SpecParam> specParams = specParamMapper.selectList(queryWrapper);

        //3.处理并返回
        if(CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return specParams;

    }

    public List<SpecGroupDTO> findSpecGroupDTOByCid(Long id) {
        //1.根据分类ID查询规格组
        List<SpecGroup> specGroups = findSpecGroupsByCids(id);
        //2.拷贝数据
        List<SpecGroupDTO> specGroupDTOS = BeanHelper.copyWithCollection(specGroups, SpecGroupDTO.class);
        //3.遍历所有SpecGroupDTO
        specGroupDTOS.forEach(specGroupDTO -> {
            //3.1 根据规格组ID查询规格参数
            List<SpecParam> specParams = findSpecParams(specGroupDTO.getId(), null, null);
            //设置组内的规格参数
            specGroupDTO.setParams(specParams);
        });
        return specGroupDTOS;
    }
}
