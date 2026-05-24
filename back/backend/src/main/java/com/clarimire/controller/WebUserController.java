package com.clarimire.controller;

import com.clarimire.entity.User;
import com.clarimire.entity.RegisterRequest;
import com.clarimire.entity.PasswordChangeRequest;
import com.clarimire.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/web/users")
@CrossOrigin(origins = "*")
public class WebUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public PageInfo<User> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) String keyword) {
        return userService.getUsers(page, pageSize, keyword, role, null);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RegisterRequest request) {
        boolean success = userService.register(request);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "用户创建成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "用户名已存在"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        User result = userService.updateUser(user);
        if (result != null) {
            return ResponseEntity.ok(Map.of("message", "更新成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Integer id) {
        boolean success = userService.resetPassword(id);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "密码重置成功，新密码: admin123"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "重置失败"));
    }
}
