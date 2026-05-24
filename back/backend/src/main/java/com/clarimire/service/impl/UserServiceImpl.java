package com.clarimire.service.impl;

import com.clarimire.entity.*;
import com.clarimire.mapper.UserMapper;
import com.clarimire.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 简单的Token存储，生产环境应使用Redis
    private static final Map<String, Integer> TOKEN_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, String> USER_TOKEN_CACHE = new ConcurrentHashMap<>();

    @Override
    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        String md5Password = DigestUtils.md5DigestAsHex((password).getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(user.getPassword())) {
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return null; // 用户被禁用
        }

        // 生成Token
        String token = UUID.randomUUID().toString();
        TOKEN_CACHE.put(token, user.getId());
        USER_TOKEN_CACHE.put(user.getId(), token);

        return new LoginResponse(token, user);
    }

    @Override
    public boolean register(RegisterRequest request) {
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            return false;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole() != null ? request.getRole() : "public");
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return true;
    }

    @Override
    public User getUserByToken(String token) {
        Integer userId = TOKEN_CACHE.get(token);
        if (userId == null) {
            return null;
        }
        return userMapper.findById(userId);
    }

    @Override
    public boolean changePassword(String token, String oldPassword, String newPassword) {
        User user = getUserByToken(token);
        if (user == null) {
            return false;
        }
        String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!md5OldPassword.equals(user.getPassword())) {
            return false;
        }

        String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));
        user.setPassword(md5NewPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return true;
    }

    @Override
    public boolean updateProfile(String token, User user) {
        User currentUser = getUserByToken(token);
        if (currentUser == null) {
            return false;
        }

        if (user.getRealName() != null) {
            currentUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            currentUser.setPhone(user.getPhone());
        }
        currentUser.setUpdatedAt(LocalDateTime.now());

        userMapper.update(currentUser);
        return true;
    }

    @Override
    public PageInfo<User> getUsers(int page, int pageSize, String username, String role, String status) {
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(userMapper.findAll(username, role, status));
    }

    @Override
    public User createUser(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return null;
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456"); // 默认密码
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        User existUser = userMapper.findById(user.getId());
        if (existUser == null) {
            return null;
        }

        if (user.getRealName() != null) {
            existUser.setRealName(user.getRealName());
        }
        if (user.getRole() != null) {
            existUser.setRole(user.getRole());
        }
        if (user.getPhone() != null) {
            existUser.setPhone(user.getPhone());
        }
        if (user.getStatus() != null) {
            existUser.setStatus(user.getStatus());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existUser.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        }
        existUser.setUpdatedAt(LocalDateTime.now());

        userMapper.update(existUser);
        return existUser;
    }

    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteById(id);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public boolean resetPassword(Integer id) {
        User user = userMapper.findById(id);
        if (user == null) {
            return false;
        }
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return true;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        User user = userMapper.findById(id);
        if (user == null) {
            return false;
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return true;
    }
}
