package com.csci334.EventHub.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class EventTicketsDTO {
    private String eventId;
    private String eventTitle;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate eventDate;
    private String location;

    private List<TicketDisplayDTO> tickets;
}
