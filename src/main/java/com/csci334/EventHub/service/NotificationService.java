package com.csci334.EventHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csci334.EventHub.entity.Notification;
import com.csci334.EventHub.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public List<Notification> getAll() {
        return repo.findAll();
    }

    public Optional<Notification> getById(Long id) {
        return repo.findById(id);
    }

    public List<Notification> getForUser(String userId) {
        return repo.findByRecipient_IdOrderBySentAtDesc(userId);
    }

    public List<Notification> getUnread(String userId) {
        return repo.findByRecipient_IdAndIsReadFalse(userId);
    }

    public List<Notification> getByEvent(String eventId) {
        return repo.findByEvent_Id(eventId);
    }

    @Transactional
    public Notification create(Notification notification) {
        return repo.save(notification);
    }

    @Transactional
    public void markAsRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setRead(true);
            repo.save(n);
        });
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
