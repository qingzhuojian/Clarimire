package com.clarimire.service.impl;

import com.clarimire.entity.User;
import com.clarimire.mapper.UserMapper;
import com.clarimire.service.UserService;
import com.clarimire.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        // MD5密码验证
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (md5Password.equals(user.getPassword()) || password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public List<User> findAll(String role) {
        return userMapper.findAll(role);
    }

    @Override
    public boolean register(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return false;
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setRole("public");
        user.setStatus(1);
        if (user.getMobileEnabled() == null) {
            user.setMobileEnabled(1);
        }
        if (user.getAllowRemoteCheckin() == null) {
            user.setAllowRemoteCheckin(0);
        }
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean createUser(User user) {
        User existUser = userMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            return false;
        }
        if (user.getPassword() != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getMobileEnabled() == null) {
            user.setMobileEnabled(1);
        }
        if (user.getAllowRemoteCheckin() == null) {
            user.setAllowRemoteCheckin(0);
        }
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean deleteUser(Integer id) {
        return userMapper.deleteById(id) > 0;
    }
}
