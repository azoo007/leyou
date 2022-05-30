package com.leyou.common.auth.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 存放JWt的载荷中的登录服务信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppInfo {
    private Long id;//服务ID
    private String serviceName;//当前服务名称
    private List<String> targetList;//当前服务的目录服务列表
}