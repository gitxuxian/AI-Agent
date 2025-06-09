package com.example.aiagent.entity.dto;


import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RequestRegister implements Serializable {


    private String email;

    private String phone;

    @NotNull
    private String password;

    private String username;

    @NotNull
    private String requirePassword;

    private static final long serialVersionUID = 1L;
}
