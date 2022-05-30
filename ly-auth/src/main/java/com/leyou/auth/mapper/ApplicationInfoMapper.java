package com.leyou.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.auth.pojo.ApplicationInfo;

import java.util.List;

public interface ApplicationInfoMapper extends BaseMapper<ApplicationInfo> {
    List<String> queryTargetList(Long id);
}
