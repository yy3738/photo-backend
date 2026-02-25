package com.photo.common.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.mvc.entity.model.SysMenuPO;
import com.photo.mvc.entity.model.SysRoleMenuPO;
import com.photo.mvc.entity.model.SysRolePO;
import com.photo.mvc.entity.model.SysUserRolePO;
import com.photo.mvc.mapper.SysMenuMapper;
import com.photo.mvc.mapper.SysRoleMapper;
import com.photo.mvc.mapper.SysRoleMenuMapper;
import com.photo.mvc.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限/角色数据源 — 从 RBAC 表查询
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());

        // user → user_role
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getUserId, userId)
        ).stream().map(SysUserRolePO::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 过滤启用的角色
        List<Long> activeRoleIds = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRolePO>()
                        .in(SysRolePO::getId, roleIds)
                        .eq(SysRolePO::getStatus, 1)
        ).stream().map(SysRolePO::getId).collect(Collectors.toList());

        if (activeRoleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // role → role_menu
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenuPO>().in(SysRoleMenuPO::getRoleId, activeRoleIds)
        ).stream().map(SysRoleMenuPO::getMenuId).collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // menu(type=button, status=1) → permission
        return sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenuPO>()
                        .in(SysMenuPO::getId, menuIds)
                        .eq(SysMenuPO::getType, "button")
                        .eq(SysMenuPO::getStatus, 1)
                        .isNotNull(SysMenuPO::getPermission)
                        .ne(SysMenuPO::getPermission, "")
        ).stream()
                .map(SysMenuPO::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());

        // user → user_role
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getUserId, userId)
        ).stream().map(SysUserRolePO::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // role(status=1) → code
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRolePO>()
                        .in(SysRolePO::getId, roleIds)
                        .eq(SysRolePO::getStatus, 1)
        ).stream()
                .map(SysRolePO::getCode)
                .collect(Collectors.toList());
    }
}
