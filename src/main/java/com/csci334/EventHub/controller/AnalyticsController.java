package com.csci334.EventHub.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csci334.EventHub.dto.analytics.AnalyticsOverviewDTO;
import com.csci334.EventHub.dto.analytics.SalesEntryDTO;
import com.csci334.EventHub.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/organizers/{organizerId}/overview")
    public AnalyticsOverviewDTO getOverview(@PathVariable String organizerId) {
        return analyticsService.getOverview(organizerId);
    }

    @GetMapping("/organizers/{organizerId}/sales")
    public List<SalesEntryDTO> getSales(@PathVariable String organizerId) {
        return analyticsService.getSales(organizerId);
    }

    @GetMapping("/events/{eventId}/overview")
    public AnalyticsOverviewDTO getEventOverview(@PathVariable String eventId) {
        return analyticsService.getEventOverview(eventId);
    }

    @GetMapping("/events/{eventId}/sales")
    public List<SalesEntryDTO> getEventSales(@PathVariable String eventId) {
        return analyticsService.getEventSales(eventId);
    }
}