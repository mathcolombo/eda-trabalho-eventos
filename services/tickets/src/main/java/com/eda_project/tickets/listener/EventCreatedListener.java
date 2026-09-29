package com.eda_project.tickets.listener;

import com.eda_project.tickets.event.EventCreatedEvent;
import com.eda_project.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventCreatedListener {

    private final TicketService ticketService;

    @RabbitListener(queues = "${rabbitmq.queue.event-created}")
    public void handle(EventCreatedEvent event) {
        log.info("EVENT_CREATED recebido: eventId={}", event.getEventId());
        ticketService.createInventory(event);
    }
}
