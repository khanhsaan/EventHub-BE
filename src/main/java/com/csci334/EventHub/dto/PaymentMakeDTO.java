package com.csci334.EventHub.dto;

import lombok.Data;

@Data
public class PaymentMakeDTO {
    private String registrationId;
    private String cardLastFour;
}
