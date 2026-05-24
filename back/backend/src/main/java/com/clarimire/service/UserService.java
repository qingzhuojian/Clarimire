package com.clarimire.service;

import com.clarimire.entity.*;
import com.github.pagehelper.PageInfo;

public interface UserService {
    LoginResponse login(String username, String password);
    boolean register(RegisterRequest request);
    User getUserByToken(String token);
    boolean changePassword(String token, String oldPassword, String newPassword);
    boolean updateProfile(String token, User user);

    PageInfo<User> getUsers(int page, int pageSize, String username, String role, String status);
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Integer id);
    User getUserById(Integer id);
    boolean resetPassword(Integer id);
    boolean updateStatus(Integer id, Integer status);
}
