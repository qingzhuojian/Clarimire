package com.clarimire.controller;

import com.clarimire.entity.User;
import com.clarimire.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public PageInfo<User> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) String status) {
        return userService.getUsers(page, pageSize, username, role, status);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        User created = userService.createUser(user);
        if (created != null) {
            return ResponseEntity.ok(created);
        }
        return ResponseEntity.status(400).body(Map.of("message", "用户名已存在"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        User updated = userService.updateUser(user);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Integer id) {
        boolean success = userService.resetPassword(id);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "密码已重置为默认密码: 123456"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "重置失败"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        boolean success = userService.updateStatus(id, status);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "状态更新成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }
}
