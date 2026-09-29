package com.eda_project.tickets.service;

import com.eda_project.tickets.controller.PurchaseRequest;
import com.eda_project.tickets.event.EventCreatedEvent;
import com.eda_project.tickets.event.EventSoldOutEvent;
import com.eda_project.tickets.event.PaymentConfirmedEvent;
import com.eda_project.tickets.event.TicketCheckedEvent;
import com.eda_project.tickets.event.TicketPurchasedEvent;
import com.eda_project.tickets.model.Ticket;
import com.eda_project.tickets.model.TicketInventory;
import com.eda_project.tickets.model.TicketStatus;
import com.eda_project.tickets.repository.TicketInventoryRepository;
import com.eda_project.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketInventoryRepository inventoryRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.events}")
    private String eventsExchange;

    @Value("${rabbitmq.exchange.tickets}")
    private String ticketsExchange;

    @Value("${rabbitmq.routing.ticket-purchased}")
    private String ticketPurchasedRoutingKey;

    @Value("${rabbitmq.routing.ticket-checked}")
    private String ticketCheckedRoutingKey;

    @Value("${rabbitmq.routing.event-soldout}")
    private String eventSoldOutRoutingKey;

    @Transactional
    public TicketInventory createInventory(EventCreatedEvent event) {
        if (inventoryRepository.findByEventId(event.getEventId()).isPresent()) {
            return inventoryRepository.findByEventId(event.getEventId()).orElseThrow();
        }

        LocalDateTime now = LocalDateTime.now();
        TicketInventory inventory = new TicketInventory();
        inventory.setEventId(event.getEventId());
        inventory.setEventName(event.getName());
        inventory.setTotalCapacity(event.getTotalCapacity());
        inventory.setReservedTickets(0);
        inventory.setConfirmedTickets(0);
        inventory.setCheckedInTickets(0);
        inventory.setBasePrice(event.getPrice());
        inventory.setCreatedAt(now);
        inventory.setUpdatedAt(now);

        TicketInventory saved = inventoryRepository.save(inventory);
        log.info("Inventory criado: eventId={} capacity={}",
                saved.getEventId(), saved.getTotalCapacity());
        return saved;
    }

    @Transactional
    public Ticket purchase(PurchaseRequest request) {
        if (request.getEventId() == null) {
            throw new IllegalArgumentException("Event id is required");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Nome do cliente e obrigatorio");
        }
        if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("Email do cliente e obrigatorio");
        }

        TicketInventory inventory = inventoryRepository.findByEventId(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory do evento nao encontrado"));

        int reserved = inventory.getReservedTickets() == null
                ? 0
                : inventory.getReservedTickets();
        int confirmed = inventory.getConfirmedTickets() == null
                ? 0
                : inventory.getConfirmedTickets();

        if (reserved + confirmed >= inventory.getTotalCapacity()) {
            throw new IllegalStateException("Event esta esgotado");
        }

        Ticket ticket = new Ticket();
        ticket.setEventId(request.getEventId());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setCustomerEmail(request.getCustomerEmail());
        ticket.setPrice(inventory.getBasePrice());
        ticket.setStatus(TicketStatus.RESERVED);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        inventory.setReservedTickets(reserved + 1);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);

        TicketPurchasedEvent event = new TicketPurchasedEvent(
                savedTicket.getId(),
                savedTicket.getEventId(),
                savedTicket.getCustomerName(),
                savedTicket.getCustomerEmail(),
                savedTicket.getPrice(),
                "TICKET_PURCHASED",
                UUID.randomUUID().toString(),
                LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                ticketsExchange,
                ticketPurchasedRoutingKey,
                event);

        log.info("Ticket reservado: ticketId={} eventId={}",
                savedTicket.getId(), savedTicket.getEventId());
        log.info("TICKET_PURCHASED publicado: ticketId={}", savedTicket.getId());

        if (reserved + 1 + confirmed >= inventory.getTotalCapacity()) {
            publishSoldOut(
                    savedTicket.getEventId(),
                    inventory.getEventName(),
                    reserved + 1 + confirmed);
        }

        return savedTicket;
    }

    @Transactional
    public void confirmPayment(PaymentConfirmedEvent event) {
        Ticket ticket = ticketRepository.findById(event.getTicketId()).orElse(null);
        if (ticket == null || ticket.getStatus() != TicketStatus.RESERVED) {
            return;
        }

        ticket.setStatus(TicketStatus.VALID);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        inventoryRepository.findByEventId(ticket.getEventId()).ifPresent(inventory -> {
            int reserved = inventory.getReservedTickets() == null
                    ? 0
                    : inventory.getReservedTickets();
            int confirmed = inventory.getConfirmedTickets() == null
                    ? 0
                    : inventory.getConfirmedTickets();
            inventory.setReservedTickets(Math.max(0, reserved - 1));
            inventory.setConfirmedTickets(confirmed + 1);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
        });

        log.info("Payment confirmado: ticketId={} status=VALID", ticket.getId());
    }

    @Transactional
    public Ticket checkIn(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket nao encontrado"));
        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalStateException("Somente tickets VALID podem fazer check-in");
        }

        ticket.setStatus(TicketStatus.CHECKED_IN);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket checkedTicket = ticketRepository.save(ticket);

        inventoryRepository.findByEventId(ticket.getEventId()).ifPresent(inventory -> {
            int checkedIn = inventory.getCheckedInTickets() == null
                    ? 0
                    : inventory.getCheckedInTickets();
            inventory.setCheckedInTickets(checkedIn + 1);
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
        });

        TicketCheckedEvent event = new TicketCheckedEvent(
                checkedTicket.getId(),
                checkedTicket.getEventId(),
            checkedTicket.getCustomerEmail(),
            "MAIN_GATE",
            LocalDateTime.now(),
                "TICKET_CHECKED",
                UUID.randomUUID().toString(),
            LocalDateTime.now());
        rabbitTemplate.convertAndSend(eventsExchange, ticketCheckedRoutingKey, event);

        log.info("Ticket com check-in: ticketId={} eventId={}",
                checkedTicket.getId(), checkedTicket.getEventId());
        return checkedTicket;
    }

    private void publishSoldOut(
            Long eventId,
            String eventName,
            Integer totalTicketsSold) {
        EventSoldOutEvent event = new EventSoldOutEvent(
                eventId,
                eventName,
                totalTicketsSold,
                "EVENT_SOLDOUT",
                UUID.randomUUID().toString(),
                LocalDateTime.now());
        rabbitTemplate.convertAndSend(eventsExchange, eventSoldOutRoutingKey, event);
        log.info("EVENT_SOLD_OUT publicado: eventId={}", eventId);
    }
}
