package com.csci334.EventHub.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.csci334.EventHub.dto.analytics.AnalyticsOverviewDTO;
import com.csci334.EventHub.dto.analytics.SalesEntryDTO;
import com.csci334.EventHub.entity.Event;
import com.csci334.EventHub.entity.Payment;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.enums.EventStatus;
import com.csci334.EventHub.entity.enums.RegistrationStatus;
import com.csci334.EventHub.entity.enums.TicketType;
import com.csci334.EventHub.repository.EventRepository;
import com.csci334.EventHub.repository.PaymentRepository;
import com.csci334.EventHub.repository.RegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AnalyticsService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private RegistrationRepository registrationRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    public AnalyticsOverviewDTO getOverview(String organizerId) {
        logger.info("Fetching overview for organizer {}", organizerId);
        List<Event> events = eventRepo.findByOrganizerId(organizerId);
        logger.debug("Found {} events for organizer {}", events.size(), organizerId);

        double totalRevenue = 0;
        int totalTickets = 0;
        int refundedTickets = 0;
        int upcomingEvents = 0;
        int cancelledEvents = 0;
        int totalEvents = events.size();
        double vipRevenue = 0, generalRevenue = 0;

        for (Event e : events) {
            if (e.getStatus() == EventStatus.CANCELLED) {
                cancelledEvents++;
            } else if (e.getStatus() == EventStatus.PUBLISHED) {
                upcomingEvents++;
            }
            List<Registration> regs = registrationRepo.findByEventId(e.getId());
            for (Registration reg : regs) {
                if (reg.getStatus() == RegistrationStatus.PAID) {
                    totalTickets++;
                    Payment payment = paymentRepo.findByRegistrationId(reg.getId()).orElse(null);
                    if (payment != null
                            && ("SUCCESS".equals(payment.getStatus()))) {
                        logger.debug("Adding payment {} of amount {}", payment.getTransactionId(), payment.getAmount());
                        totalRevenue += payment.getAmount();
                        if (reg.getTicket().getTicketType() == TicketType.VIP)
                            vipRevenue += payment.getAmount();
                        else
                            generalRevenue += payment.getAmount();
                    }
                } else if (reg.getStatus() == RegistrationStatus.REFUNDED) {
                    refundedTickets++;
                }
            }
        }

        List<AnalyticsOverviewDTO.RevenueByType> revenueList = List.of(
                new AnalyticsOverviewDTO.RevenueByType("General", generalRevenue),
                new AnalyticsOverviewDTO.RevenueByType("VIP", vipRevenue));

        return new AnalyticsOverviewDTO(totalRevenue, totalTickets, refundedTickets, totalEvents, upcomingEvents,
                cancelledEvents,
                revenueList);
    }

    public List<SalesEntryDTO> getSales(String organizerId) {
        logger.info("Fetching sales data for organizer {}", organizerId);
        List<Event> events = eventRepo.findByOrganizerId(organizerId);
        Map<String, SalesEntryDTO> dateMap = new TreeMap<>();

        for (Event event : events) {
            logger.debug("Processing event: {}", event.getTitle());
            List<Registration> regs = registrationRepo.findByEventId(event.getId());

            for (Registration reg : regs) {
                if (reg.getStatus() == RegistrationStatus.PAID) {
                    Payment payment = paymentRepo.findByRegistrationId(reg.getId()).orElse(null);

                    if (payment != null && payment.getPaidAt() != null) {
                        String dateKey = payment.getPaidAt().toLocalDate().toString();
                        dateMap.putIfAbsent(dateKey, new SalesEntryDTO(dateKey, 0, 0));
                        SalesEntryDTO entry = dateMap.get(dateKey);

                        if (reg.getTicket().getTicketType() == TicketType.VIP) {
                            entry.setVip(entry.getVip() + 1);
                            logger.debug("VIP ticket sold on {}: total VIP for the day: {}", dateKey, entry.getVip());
                        } else {
                            entry.setGeneral(entry.getGeneral() + 1);
                            logger.debug("General ticket sold on {}: total General for the day: {}", dateKey,
                                    entry.getGeneral());
                        }
                    }
                }
            }
        }

        logger.info("Returning {} sales entries for organizer {}", dateMap.size(), organizerId);
        return new ArrayList<>(dateMap.values());
    }

    @Scheduled(fixedRate = 5000)
    public void broadcastOrganizerAnalytics() {
        List<String> organizerIds = getAllOrganizerIds();
        for (String organizerId : organizerIds) {
            AnalyticsOverviewDTO overview = getOverview(organizerId);
            List<SalesEntryDTO> sales = getSales(organizerId);

            messagingTemplate.convertAndSend("/topic/analytics/" + organizerId, overview);
            for (SalesEntryDTO entry : sales) {
                messagingTemplate.convertAndSend("/topic/sales/" + organizerId, entry);
            }
        }
    }

    // Broadcast event-specific overview and sales every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void broadcastEventAnalytics() {
        List<String> eventIds = getAllEventIds();
        for (String eventId : eventIds) {
            AnalyticsOverviewDTO overview = getEventOverview(eventId);
            List<SalesEntryDTO> sales = getEventSales(eventId);

            messagingTemplate.convertAndSend("/topic/analytics/event/" + eventId, overview);
            for (SalesEntryDTO entry : sales) {
                messagingTemplate.convertAndSend("/topic/sales/event/" + eventId, entry);
            }
        }
    }

    // Event-specific analytics computation
    public AnalyticsOverviewDTO computeOverviewForSingleEvent(String eventId) {
        Event event = eventRepo.findById(eventId).orElseThrow();
        double totalRevenue = 0;
        int totalTickets = 0;
        int refundedTickets = 0;
        double vipRevenue = 0, generalRevenue = 0;

        List<Registration> regs = registrationRepo.findByEventId(eventId);
        for (Registration reg : regs) {
            if (reg.getStatus() == RegistrationStatus.PAID) {
                totalTickets++;
                Payment payment = paymentRepo.findByRegistrationId(reg.getId()).orElse(null);
                if (payment != null
                        && ("SUCCESS".equals(payment.getStatus()))) {
                    totalRevenue += payment.getAmount();
                    if (reg.getTicket().getTicketType() == TicketType.VIP)
                        vipRevenue += payment.getAmount();
                    else
                        generalRevenue += payment.getAmount();
                }
            }
            if (reg.getStatus() == RegistrationStatus.REFUNDED) {
                refundedTickets++;
            }
        }

        List<AnalyticsOverviewDTO.RevenueByType> revenueList = List.of(
                new AnalyticsOverviewDTO.RevenueByType("General", generalRevenue),
                new AnalyticsOverviewDTO.RevenueByType("VIP", vipRevenue));

        return new AnalyticsOverviewDTO(totalRevenue, totalTickets, refundedTickets, 1, 0, 0, revenueList);
    }

    public List<SalesEntryDTO> computeSalesForSingleEvent(String eventId) {
        Map<String, SalesEntryDTO> dateMap = new TreeMap<>();
        List<Registration> regs = registrationRepo.findByEventId(eventId);

        for (Registration reg : regs) {
            if (reg.getStatus() == RegistrationStatus.PAID) {
                Payment payment = paymentRepo.findByRegistrationId(reg.getId()).orElse(null);

                if (payment != null && payment.getPaidAt() != null) {
                    String dateKey = payment.getPaidAt().toLocalDate().toString();
                    dateMap.putIfAbsent(dateKey, new SalesEntryDTO(dateKey, 0, 0));
                    SalesEntryDTO entry = dateMap.get(dateKey);

                    if (reg.getTicket().getTicketType() == TicketType.VIP) {
                        entry.setVip(entry.getVip() + 1);
                    } else {
                        entry.setGeneral(entry.getGeneral() + 1);
                    }
                }
            }
        }

        return new ArrayList<>(dateMap.values());
    }

    public List<String> getAllOrganizerIds() {
        return eventRepo.findDistinctOrganizerIds();
    }

    public List<String> getAllEventIds() {
        return eventRepo.findAllEventIds();
    }

    public AnalyticsOverviewDTO getEventOverview(String eventId) {
        return computeOverviewForSingleEvent(eventId);
    }

    public List<SalesEntryDTO> getEventSales(String eventId) {
        return computeSalesForSingleEvent(eventId);
    }

}
