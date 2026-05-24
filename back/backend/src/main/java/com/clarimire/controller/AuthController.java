package com.clarimire.controller;

import com.clarimire.entity.*;
import com.clarimire.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.getUsername(), request.getPassword());
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        boolean success = userService.register(request);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "注册成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "用户名已存在"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        token = token.substring(7);
        User user = userService.getUserByToken(token);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body(Map.of("message", "Token无效"));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody PasswordChangeRequest request) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        token = token.substring(7);
        boolean success = userService.changePassword(token, request.getOldPassword(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "旧密码错误"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody User user) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        token = token.substring(7);
        boolean success = userService.updateProfile(token, user);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "个人信息更新成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }
}
