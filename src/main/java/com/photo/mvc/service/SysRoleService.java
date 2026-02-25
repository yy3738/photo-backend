package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.exception.BizException;
import com.photo.common.result.PageQuery;
import com.photo.common.result.PageResult;
import com.photo.mvc.entity.model.SysRoleMenuPO;
import com.photo.mvc.entity.model.SysRolePO;
import com.photo.mvc.entity.model.SysUserPO;
import com.photo.mvc.entity.model.SysUserRolePO;
import com.photo.mvc.entity.req.AdminRoleReq;
import com.photo.mvc.entity.req.AdminStatusReq;
import com.photo.mvc.entity.vo.RoleVO;
import com.photo.mvc.mapper.SysRoleMapper;
import com.photo.mvc.mapper.SysRoleMenuMapper;
import com.photo.mvc.mapper.SysUserMapper;
import com.photo.mvc.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserMapper sysUserMapper;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PageResult<RoleVO> listRoles(PageQuery query) {
        Page<SysRolePO> page = new Page<>(query.getPage(), query.getPageSize());
        sysRoleMapper.selectPage(page,
                new LambdaQueryWrapper<SysRolePO>().orderByAsc(SysRolePO::getSort));

        List<RoleVO> list = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public RoleVO getRoleDetail(Long id) {
        SysRolePO role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }
        return toVO(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(AdminRoleReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();

        // code 唯一校验
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRolePO>().eq(SysRolePO::getCode, req.getCode()));
        if (count > 0) {
            throw new BizException(400, "角色编码已存在");
        }

        SysRolePO role = new SysRolePO();
        role.setName(req.getName());
        role.setCode(req.getCode());
        role.setSort(req.getSort() != null ? req.getSort() : 0);
        role.setStatus(1);
        role.setRemark(req.getRemark());
        role.setCreateBy(adminId);
        role.setUpdateBy(adminId);
        sysRoleMapper.insert(role);

        // 批量写 role_menu
        saveRoleMenus(role.getId(), req.getMenuIds(), adminId);

        return toVO(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(Long id, AdminRoleReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();

        SysRolePO role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }

        // code 唯一校验（排除自身）
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRolePO>()
                        .eq(SysRolePO::getCode, req.getCode())
                        .ne(SysRolePO::getId, id));
        if (count > 0) {
            throw new BizException(400, "角色编码已存在");
        }

        role.setName(req.getName());
        role.setCode(req.getCode());
        if (req.getSort() != null) {
            role.setSort(req.getSort());
        }
        role.setRemark(req.getRemark());
        role.setUpdateBy(adminId);
        sysRoleMapper.updateById(role);

        // menuIds 全量替换（先删后增）
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenuPO>().eq(SysRoleMenuPO::getRoleId, id));
        saveRoleMenus(id, req.getMenuIds(), adminId);

        return toVO(role);
    }

    public void updateRoleStatus(Long id, AdminStatusReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysRolePO role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }
        role.setStatus(req.getStatus());
        role.setUpdateBy(adminId);
        sysRoleMapper.updateById(role);
    }

    public void deleteRole(Long id) {
        SysRolePO role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }

        Long userCount = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getRoleId, id));
        if (userCount > 0) {
            throw new BizException(409, "角色已分配用户，不允许删除");
        }

        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenuPO>().eq(SysRoleMenuPO::getRoleId, id));
        sysRoleMapper.deleteById(id);
    }

    public List<String> getUserRoleIds(Long userId) {
        List<SysUserRolePO> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getUserId, userId));
        return userRoles.stream()
                .map(ur -> String.valueOf(ur.getRoleId()))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        Long adminId = StpUtil.getLoginIdAsLong();

        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }

        // 全量替换用户角色
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRolePO>().eq(SysUserRolePO::getUserId, userId));

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRolePO ur = new SysUserRolePO();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setCreateBy(adminId);
                ur.setUpdateBy(adminId);
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds, Long adminId) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenuPO rm = new SysRoleMenuPO();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            rm.setCreateBy(adminId);
            rm.setUpdateBy(adminId);
            sysRoleMenuMapper.insert(rm);
        }
    }

    private RoleVO toVO(SysRolePO role) {
        RoleVO vo = new RoleVO();
        vo.setId(String.valueOf(role.getId()));
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setSort(role.getSort());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setCreatedAt(role.getCreateTime() != null ? role.getCreateTime().format(ISO_FMT) : null);

        // 查询关联菜单ID
        List<SysRoleMenuPO> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenuPO>().eq(SysRoleMenuPO::getRoleId, role.getId()));
        vo.setMenuIds(roleMenus.stream()
                .map(rm -> String.valueOf(rm.getMenuId()))
                .collect(Collectors.toList()));

        return vo;
    }
}
