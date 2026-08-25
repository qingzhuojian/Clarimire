package com.clarimire.controller;

import com.clarimire.entity.User;
import com.clarimire.service.UserService;
import com.clarimire.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");
        String clientType = params.getOrDefault("clientType", "web");

        User user = userService.login(username, password);
        if (user == null) {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            return result;
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            result.put("code", 403);
            result.put("message", "账号已禁用");
            return result;
        }

        if ("mobile".equals(clientType)) {
            if ("admin".equals(user.getRole())) {
                result.put("code", 403);
                result.put("message", "管理员请使用 Web 端登录");
                return result;
            }
            if (user.getMobileEnabled() != null && user.getMobileEnabled() == 0) {
                result.put("code", 403);
                result.put("message", "该账号未开通移动端");
                return result;
            }
            if (!"inspector".equals(user.getRole()) && !"public".equals(user.getRole())) {
                result.put("code", 403);
                result.put("message", "该账号不支持移动端登录");
                return result;
            }
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        result.put("code", 200);
        result.put("message", "登录成功");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("userId", user.getId());
        data.put("allowRemoteCheckin", user.getAllowRemoteCheckin());
        data.put("clientType", clientType);
        result.put("data", data);
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        if (userService.register(user)) {
            result.put("code", 200);
            result.put("message", "注册成功");
        } else {
            result.put("code", 400);
            result.put("message", "用户名已存在");
        }
        return result;
    }

    @GetMapping("/userinfo")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            if (user != null) {
                result.put("code", 200);
                user.setPassword(null);
                result.put("data", user);
            } else {
                result.put("code", 404);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", "无效的token");
        }
        return result;
    }
}
