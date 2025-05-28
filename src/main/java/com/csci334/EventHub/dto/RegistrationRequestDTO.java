package com.csci334.EventHub.dto;

import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.TicketType;
import com.csci334.EventHub.entity.Event;
import lombok.Data;
import java.util.List;

@Data
public class RegistrationRequestDTO {
    private String eventId;
    private String attendeeId;
    private TicketType ticketType;

}