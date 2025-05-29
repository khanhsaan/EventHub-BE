package com.csci334.EventHub.dto;

import java.time.LocalDate;

import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketType;

import lombok.Data;

@Data
public class MyRegistrationDTO {
    private String registrationId;
    private String attendeeId;
    private String eventTitle;;
    private String location;
    private LocalDate date;
    private String time;
    private TicketType ticketType;
    private double amountDue;
    private RegistrationStatus status;
    private String ticketCode;
    private String cardLastFour;
}
