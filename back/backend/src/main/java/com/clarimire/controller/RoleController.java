package com.clarimire.controller;

import com.clarimire.entity.Role;
import com.clarimire.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<Role> getAll() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable Integer id) {
        return roleService.getRoleById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Role role) {
        Role created = roleService.createRole(role);
        if (created != null) {
            return ResponseEntity.ok(created);
        }
        return ResponseEntity.status(400).body(Map.of("message", "角色标识已存在"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Role role) {
        role.setId(id);
        Role updated = roleService.updateRole(role);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
