package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.common.exception.BizException;
import com.photo.mvc.entity.model.SysOrgPO;
import com.photo.mvc.entity.model.SysUserPO;
import com.photo.mvc.entity.req.AdminOrgReq;
import com.photo.mvc.entity.req.AdminStatusReq;
import com.photo.mvc.entity.vo.OrgVO;
import com.photo.mvc.mapper.SysOrgMapper;
import com.photo.mvc.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysOrgService {

    private final SysOrgMapper sysOrgMapper;
    private final SysUserMapper sysUserMapper;

    public List<OrgVO> getOrgTree() {
        List<SysOrgPO> all = sysOrgMapper.selectList(
                new LambdaQueryWrapper<SysOrgPO>().orderByAsc(SysOrgPO::getSort));
        return buildTree(all);
    }

    public OrgVO createOrg(AdminOrgReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysOrgPO org = new SysOrgPO();
        org.setName(req.getName());
        org.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        org.setSort(req.getSort() != null ? req.getSort() : 0);
        org.setStatus(1);
        org.setCreateBy(adminId);
        org.setUpdateBy(adminId);
        sysOrgMapper.insert(org);
        return toVO(org);
    }

    public OrgVO updateOrg(Long id, AdminOrgReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysOrgPO org = sysOrgMapper.selectById(id);
        if (org == null) {
            throw new BizException(404, "组织不存在");
        }
        org.setName(req.getName());
        if (req.getParentId() != null) {
            org.setParentId(req.getParentId());
        }
        if (req.getSort() != null) {
            org.setSort(req.getSort());
        }
        org.setUpdateBy(adminId);
        sysOrgMapper.updateById(org);
        return toVO(org);
    }

    public void updateOrgStatus(Long id, AdminStatusReq req) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysOrgPO org = sysOrgMapper.selectById(id);
        if (org == null) {
            throw new BizException(404, "组织不存在");
        }
        org.setStatus(req.getStatus());
        org.setUpdateBy(adminId);
        sysOrgMapper.updateById(org);

        // 禁用时递归禁用子组织
        if (req.getStatus() == 0) {
            disableChildren(id, adminId);
        }
    }

    public void deleteOrg(Long id) {
        SysOrgPO org = sysOrgMapper.selectById(id);
        if (org == null) {
            throw new BizException(404, "组织不存在");
        }

        Long childCount = sysOrgMapper.selectCount(
                new LambdaQueryWrapper<SysOrgPO>().eq(SysOrgPO::getParentId, id));
        if (childCount > 0) {
            throw new BizException(409, "存在子组织，不允许删除");
        }

        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUserPO>().eq(SysUserPO::getOrgId, id));
        if (userCount > 0) {
            throw new BizException(409, "组织下有用户，不允许删除");
        }

        sysOrgMapper.deleteById(id);
    }

    public void assignUserOrg(Long userId, Long orgId) {
        Long adminId = StpUtil.getLoginIdAsLong();
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (orgId != null) {
            SysOrgPO org = sysOrgMapper.selectById(orgId);
            if (org == null) {
                throw new BizException(404, "组织不存在");
            }
        }
        user.setOrgId(orgId);
        user.setUpdateBy(adminId);
        sysUserMapper.updateById(user);
    }

    private void disableChildren(Long parentId, Long adminId) {
        List<SysOrgPO> children = sysOrgMapper.selectList(
                new LambdaQueryWrapper<SysOrgPO>().eq(SysOrgPO::getParentId, parentId));
        for (SysOrgPO child : children) {
            child.setStatus(0);
            child.setUpdateBy(adminId);
            sysOrgMapper.updateById(child);
            disableChildren(child.getId(), adminId);
        }
    }

    private List<OrgVO> buildTree(List<SysOrgPO> all) {
        Map<Long, List<SysOrgPO>> grouped = all.stream()
                .collect(Collectors.groupingBy(o -> o.getParentId() != null ? o.getParentId() : 0L));
        return buildChildren(grouped, 0L);
    }

    private List<OrgVO> buildChildren(Map<Long, List<SysOrgPO>> grouped, Long parentId) {
        List<SysOrgPO> children = grouped.getOrDefault(parentId, Collections.emptyList());
        return children.stream().map(o -> {
            OrgVO vo = toVO(o);
            vo.setChildren(buildChildren(grouped, o.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private OrgVO toVO(SysOrgPO org) {
        OrgVO vo = new OrgVO();
        vo.setId(String.valueOf(org.getId()));
        vo.setName(org.getName());
        vo.setParentId(org.getParentId() != null ? String.valueOf(org.getParentId()) : null);
        vo.setSort(org.getSort());
        vo.setStatus(org.getStatus());
        return vo;
    }
}
