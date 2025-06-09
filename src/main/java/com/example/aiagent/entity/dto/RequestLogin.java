package com.example.aiagent.entity.dto;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RequestLogin implements Serializable {

    private String email;

    private String phone;

    @NotNull
    private String password;
}
