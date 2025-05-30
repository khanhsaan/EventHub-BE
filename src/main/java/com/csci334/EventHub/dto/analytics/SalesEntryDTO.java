package com.csci334.EventHub.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesEntryDTO {
    private String date;
    private int general;
    private int vip;
}
