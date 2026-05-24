package com.clarimire.entity;

import lombok.Data;

@Data
public class LoginResponse {
    private Integer userId;
    private String username;
    private String realName;
    private String role;
    private String token;
    private String message;

    public LoginResponse() {}

    public LoginResponse(Integer userId, String username, String realName, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.token = token;
    }

    public LoginResponse(String token, User user) {
        this.token = token;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.role = user.getRole();
    }
}
