package com.easy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 角色权限管理器
 */
@Component
public class RolePermissionManager {

    // 构造函数注入
    private final RolePathPermissionsConfig rolePathPermissionsConfig;

    @Autowired
    public RolePermissionManager(RolePathPermissionsConfig rolePathPermissionsConfig) {
        this.rolePathPermissionsConfig = rolePathPermissionsConfig;
    }

    // 判断当前角色是否有权限访问请求的路径（自定义的路径匹配权限控制）
    public boolean hasPermission(String role, String requestURI) {
        Map<String, List<String>> permissions = rolePathPermissionsConfig.getPermissions();
        List<String> allowedPaths = permissions.get(role);
        if (allowedPaths != null) {
            for (String path : allowedPaths) {
                if (requestURI.startsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }
}

