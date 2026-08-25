package com.clarimire.service;

import com.clarimire.entity.User;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User login(String username, String password);
    User findById(Integer id);
    List<User> findAll(String role);
    boolean register(User user);
    boolean createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(Integer id);
}
