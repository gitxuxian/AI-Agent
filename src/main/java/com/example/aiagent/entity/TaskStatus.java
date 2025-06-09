package com.example.aiagent.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TaskStatus {
    private String taskId;
    private String fileName;
    private String status;
    private String message;
    private String fileUrl;
    private Long fileSize;
    private Date createTime;
    private Date updateTime;

    public TaskStatus() {
    }

    public TaskStatus(String taskId, String fileName, String status, String message) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.status = status;
        this.message = message;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}