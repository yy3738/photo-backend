package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.common.exception.BizException;
import com.photo.mvc.entity.model.SysMenuPO;
import com.photo.mvc.entity.model.SysRoleMenuPO;
import com.photo.mvc.entity.req.AdminMenuReq;
import com.photo.mvc.entity.vo.MenuVO;
import com.photo.mvc.mapper.SysMenuMapper;
import com.photo.mvc.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    public List<MenuVO> getMenuTree() {
        List<SysMenuPO> all = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenuPO>().orderByAsc(SysMenuPO::getSort));
        return buildTree(all);
    }

    public MenuVO createMenu(AdminMenuReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysMenuPO menu = new SysMenuPO();
        menu.setName(req.getName());
        menu.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        menu.setType(req.getType());
        menu.setPath(req.getPath());
        menu.setPermission(req.getPermission());
        menu.setIcon(req.getIcon());
        menu.setSort(req.getSort() != null ? req.getSort() : 0);
        menu.setStatus(1);
        menu.setCreateBy(adminId);
        menu.setUpdateBy(adminId);
        sysMenuMapper.insert(menu);
        return toVO(menu);
    }

    public MenuVO updateMenu(Long id, AdminMenuReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysMenuPO menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(404, "菜单不存在");
        }
        menu.setName(req.getName());
        if (req.getParentId() != null) {
            menu.setParentId(req.getParentId());
        }
        menu.setType(req.getType());
        menu.setPath(req.getPath());
        menu.setPermission(req.getPermission());
        menu.setIcon(req.getIcon());
        if (req.getSort() != null) {
            menu.setSort(req.getSort());
        }
        menu.setUpdateBy(adminId);
        sysMenuMapper.updateById(menu);
        return toVO(menu);
    }

    public void deleteMenu(Long id) {
        SysMenuPO menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(404, "菜单不存在");
        }

        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenuPO>().eq(SysMenuPO::getParentId, id));
        if (childCount > 0) {
            throw new BizException(409, "存在子菜单，不允许删除");
        }

        // 清理 role_menu 关联
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenuPO>().eq(SysRoleMenuPO::getMenuId, id));
        sysMenuMapper.deleteById(id);
    }

    private List<MenuVO> buildTree(List<SysMenuPO> all) {
        Map<Long, List<SysMenuPO>> grouped = all.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : 0L));
        return buildChildren(grouped, 0L);
    }

    private List<MenuVO> buildChildren(Map<Long, List<SysMenuPO>> grouped, Long parentId) {
        List<SysMenuPO> children = grouped.getOrDefault(parentId, Collections.emptyList());
        return children.stream().map(m -> {
            MenuVO vo = toVO(m);
            vo.setChildren(buildChildren(grouped, m.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private MenuVO toVO(SysMenuPO menu) {
        MenuVO vo = new MenuVO();
        vo.setId(String.valueOf(menu.getId()));
        vo.setName(menu.getName());
        vo.setParentId(menu.getParentId() != null ? String.valueOf(menu.getParentId()) : null);
        vo.setType(menu.getType());
        vo.setPath(menu.getPath());
        vo.setPermission(menu.getPermission());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setStatus(menu.getStatus());
        return vo;
    }
}
