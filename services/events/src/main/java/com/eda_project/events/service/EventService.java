package com.eda_project.events.service;

import com.eda_project.events.event.EventCreatedEvent;
import com.eda_project.events.model.Event;
import com.eda_project.events.model.EventStatus;
import com.eda_project.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.events}")
    private String exchangeName;

    @Value("${rabbitmq.routing.event-created}")
    private String eventCreatedRoutingKey;

    @Transactional
    public Event createEvent(Event event) {
        validate(event);

        LocalDateTime now = LocalDateTime.now();
        event.setStatus(EventStatus.ATIVO);
        event.setConfirmedTickets(0);
        event.setCheckedInTickets(0);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        Event savedEvent = eventRepository.save(event);

        EventCreatedEvent eventMessage = new EventCreatedEvent(
                savedEvent.getId(),
                savedEvent.getName(),
                savedEvent.getEventDate(),
                savedEvent.getMaxCapacity(),
                savedEvent.getBasePrice());

        rabbitTemplate.convertAndSend(
                exchangeName,
                eventCreatedRoutingKey,
                eventMessage);

        log.info("Event created: id={} name={}",
                savedEvent.getId(), savedEvent.getName());
        log.info("EVENT_CREATED published: eventId={}", savedEvent.getId());

        return savedEvent;
    }

    private void validate(Event event) {
        if (event.getName() == null || event.getName().isBlank()) {
            throw new IllegalArgumentException("Event name is required");
        }
        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (event.getMaxCapacity() == null || event.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be greater than zero");
        }
        if (event.getBasePrice() == null || event.getBasePrice().signum() < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
    }
}
