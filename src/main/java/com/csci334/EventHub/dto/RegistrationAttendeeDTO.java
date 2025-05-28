package com.csci334.EventHub.dto;

import java.util.List;

import lombok.Data;

@Data
public class RegistrationAttendeeDTO {
    private String registrationId;
    private String name;
    private String email;
    private String status;
    private List<String> tickets;

    // Constructors
    public RegistrationAttendeeDTO(String registrationId, String name, String email, String status,
            List<String> tickets) {
        this.registrationId = registrationId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.tickets = tickets;
    }
}
