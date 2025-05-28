package com.csci334.EventHub.dto;

import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.EventType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class EventOutDTO {
    private String id;
    private String title;
    private String description;
    private String shortDescription;
    private String location;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime lastUpdatedAt;
    private EventType eventType;
    private EventStatus status;
    private Double generalPrice;
    private Double vipPrice;
    private Integer generalTicketLimit;
    private Integer vipTicketLimit;
    private Integer generalTicketsRemaining;
    private Integer vipTicketsRemaining;
    private String imageUrl;
    private String organizerId;
}
