package com.clarimire.service;

import com.clarimire.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Integer id);
    Role createRole(Role role);
    Role updateRole(Role role);
    void deleteRole(Integer id);
}
