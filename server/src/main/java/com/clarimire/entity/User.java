package com.clarimire.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String role;
    private Integer status;
    private Integer allowRemoteCheckin;
    private Integer mobileEnabled;
    private Date createTime;
    private Date updateTime;
}
