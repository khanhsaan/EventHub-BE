package com.csci334.EventHub.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketType;

import lombok.Data;

@Data
public class RegistrationOutDTO {
    private String registrationId;
    private String attendeeId;
    private String fullName;
    private String email;
    private TicketType ticketRequested;
    private RegistrationStatus registrationStatus;

}
