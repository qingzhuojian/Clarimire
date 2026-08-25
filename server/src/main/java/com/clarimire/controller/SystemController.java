package com.clarimire.controller;

import com.clarimire.entity.CheckinPolicy;
import com.clarimire.entity.User;
import com.clarimire.service.IssueService;
import com.clarimire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private UserService userService;

    @Autowired
    private IssueService issueService;

    @GetMapping("/users")
    public Map<String, Object> listUsers(@RequestParam(required = false) String role) {
        Map<String, Object> result = new HashMap<>();
        List<User> list = userService.findAll(role);
        list.forEach(u -> u.setPassword(null));
        result.put("code", 200);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        if (userService.createUser(user)) {
            result.put("code", 200);
            result.put("message", "创建成功");
        } else {
            result.put("code", 400);
            result.put("message", "创建失败，用户名可能已存在");
        }
        return result;
    }

    @PutMapping("/users")
    public Map<String, Object> updateUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        if (userService.updateUser(user)) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 400);
            result.put("message", "更新失败");
        }
        return result;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (userService.deleteUser(id)) {
            result.put("code", 200);
            result.put("message", "删除成功");
        } else {
            result.put("code", 400);
            result.put("message", "删除失败");
        }
        return result;
    }

    @GetMapping("/checkin-policy")
    public Map<String, Object> getPolicy() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", issueService.getPolicy());
        return result;
    }

    @PutMapping("/checkin-policy")
    public Map<String, Object> updatePolicy(@RequestBody CheckinPolicy policy) {
        Map<String, Object> result = new HashMap<>();
        if (issueService.updatePolicy(policy)) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 400);
            result.put("message", "更新失败");
        }
        return result;
    }

    @GetMapping("/reservoir-locations")
    public Map<String, Object> getReservoirLocations() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", issueService.getReservoirLocations());
        return result;
    }
}
