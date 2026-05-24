package com.clarimire.service.impl;

import com.clarimire.entity.Role;
import com.clarimire.mapper.RoleMapper;
import com.clarimire.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.findAll();
    }

    @Override
    public Role getRoleById(Integer id) {
        return roleMapper.findById(id);
    }

    @Override
    public Role createRole(Role role) {
        Role existRole = roleMapper.findByRoleKey(role.getRoleKey());
        if (existRole != null) {
            return null;
        }
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role updateRole(Role role) {
        Role existRole = roleMapper.findById(role.getId());
        if (existRole == null) {
            return null;
        }
        if (role.getRoleName() != null) {
            existRole.setRoleName(role.getRoleName());
        }
        if (role.getDescription() != null) {
            existRole.setDescription(role.getDescription());
        }
        if (role.getPermissions() != null) {
            existRole.setPermissions(role.getPermissions());
        }
        existRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.update(existRole);
        return existRole;
    }

    @Override
    public void deleteRole(Integer id) {
        roleMapper.deleteById(id);
    }
}
