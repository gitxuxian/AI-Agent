package com.example.aiagent.entity.vo;

import lombok.Data;

import java.io.Serializable;

// 添加LoginUserVO类定义
@Data
public class LoginUserVO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String phone;
}