package com.csci334.EventHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.csci334.EventHub.entity.Registration;
import com.csci334.EventHub.entity.enums.RegistrationStatus;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, String> {

    List<Registration> findByAttendeeId(String attendeeId);

    List<Registration> findByAttendeeIdAndEventId(String attendeeId, String eventId);

    boolean existsByEventIdAndAttendeeId(String eventId, String attendeeId);

    List<Registration> findByEventId(String eventId);

}
