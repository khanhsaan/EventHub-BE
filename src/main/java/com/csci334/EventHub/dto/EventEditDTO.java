package com.csci334.EventHub.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.csci334.EventHub.entity.enums.EventType;

import lombok.Data;

@Data
public class EventEditDTO {
    private String title;
    private String shortDescription;
    private String description;
    private String location;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private EventType eventType;
    private String imageUrl;

    private Double generalPrice;
    private Double vipPrice;

    private Integer generalTicketLimit;
    private Integer vipTicketLimit;

    private Integer generalTicketsRemaining;
    private Integer vipTicketsRemaining;

    // Getters and Setters
    // (Use Lombok @Data or @Getter/@Setter if preferred)
}
