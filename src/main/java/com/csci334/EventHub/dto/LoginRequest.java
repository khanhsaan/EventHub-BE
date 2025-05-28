package com.csci334.EventHub.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}