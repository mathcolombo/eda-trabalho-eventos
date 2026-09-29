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

        log.info("Evento criado: id={} name={}",
                savedEvent.getId(), savedEvent.getName());
        log.info("EVENT_CREATED publicado: eventId={}", savedEvent.getId());

        return savedEvent;
    }

    @Transactional
    public void confirmTicket(Long eventId) {
        eventRepository.findById(eventId).ifPresent(event -> {
            int confirmed = event.getConfirmedTickets() == null
                    ? 0
                    : event.getConfirmedTickets();
            event.setConfirmedTickets(confirmed + 1);
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
        });
    }

    @Transactional
    public void registerCheckIn(Long eventId) {
        eventRepository.findById(eventId).ifPresent(event -> {
            int checkedIn = event.getCheckedInTickets() == null
                    ? 0
                    : event.getCheckedInTickets();
            event.setCheckedInTickets(checkedIn + 1);
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
        });
    }

    @Transactional
    public void markSoldOut(Long eventId) {
        eventRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(EventStatus.ESGOTADO);
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
        });
    }

    private void validate(Event event) {
        if (event.getName() == null || event.getName().isBlank()) {
            throw new IllegalArgumentException("Nome do evento e obrigatorio");
        }
        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("Data do evento e obrigatoria");
        }
        if (event.getMaxCapacity() == null || event.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("A capacidade maxima deve ser maior que zero");
        }
        if (event.getBasePrice() == null || event.getBasePrice().signum() < 0) {
            throw new IllegalArgumentException("O preco base nao pode ser negativo");
        }
    }
}
