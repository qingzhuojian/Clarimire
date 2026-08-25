package com.clarimire.entity;

import lombok.Data;
import java.util.Date;

@Data
public class OperationLog {
    private Integer id;
    private String username;
    private String operation;
    private String module;
    private String detail;
    private String ip;
    private Date createTime;
}
