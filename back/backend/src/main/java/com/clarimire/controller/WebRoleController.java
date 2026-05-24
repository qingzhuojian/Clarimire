package com.clarimire.controller;

import com.clarimire.entity.Role;
import com.clarimire.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/roles")
@CrossOrigin(origins = "*")
public class WebRoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<Role> list() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable Integer id) {
        return roleService.getRoleById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Role role) {
        roleService.createRole(role);
        return ResponseEntity.ok(Map.of("message", "角色创建成功"));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<?> updatePermissions(@PathVariable Integer id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return ResponseEntity.ok(Map.of("message", "权限更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
