package com.csci334.EventHub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.EventType;

import lombok.Data;

@Data
public class EventCreationDTO {
    private String title;
    private String description;
    private String shortDescription;
    private String location;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private EventType eventType;
    private EventStatus status;
    private Double generalPrice;
    private Double vipPrice;
    private Integer generalTicketLimit;
    private Integer vipTicketLimit;
    private String imageUrl;
    private String organizerId; // This will be used to find the User entity
}