package com.clarimire.entity;

import lombok.Data;

@Data
public class AssignRequest {
    private Integer recordId;
    private String inspector;
    private String note;
}
