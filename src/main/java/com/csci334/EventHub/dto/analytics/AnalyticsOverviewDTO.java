package com.csci334.EventHub.dto.analytics;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsOverviewDTO {
    private double totalRevenue;
    private int totalTickets;
    private int refundedTickets;
    private int totalEvents;
    private int upcomingEvents;
    private int cancelledEvents;
    private List<RevenueByType> revenueByType;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueByType {
        private String type;
        private double value;
    }
}
