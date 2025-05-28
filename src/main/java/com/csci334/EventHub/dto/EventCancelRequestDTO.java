package com.csci334.EventHub.dto;

// Simple DTO to hold the password for the cancel request
public class EventCancelRequestDTO {
    private String password;

    // Default constructor (needed for JSON deserialization)
    public EventCancelRequestDTO() {
    }

    // Constructor with password
    public EventCancelRequestDTO(String password) {
        this.password = password;
    }

    // Getter
    public String getPassword() {
        return password;
    }

    // Setter
    public void setPassword(String password) {
        this.password = password;
    }
}