package com.csci334.EventHub.dto;

import lombok.Data;

@Data
public class TicketDisplayDTO {
    private String ticketType;
    private Double price;
    private String ticketCode;
}
