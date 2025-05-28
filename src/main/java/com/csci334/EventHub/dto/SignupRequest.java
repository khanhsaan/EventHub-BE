package com.csci334.EventHub.dto;

import com.csci334.EventHub.entity.enums.Role;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;
}