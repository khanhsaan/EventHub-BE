package com.csci334.EventHub.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private LocalDateTime sentAt;
    private boolean read;
    private String recipientName;
}
