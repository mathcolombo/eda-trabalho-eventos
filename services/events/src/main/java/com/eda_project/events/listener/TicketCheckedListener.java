package com.eda_project.events.listener;

import com.eda_project.events.event.TicketCheckedEvent;
import com.eda_project.events.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketCheckedListener {

    private final EventService eventService;

    @RabbitListener(queues = "events.checkin.queue")
    public void handle(TicketCheckedEvent event) {
        log.info("TICKET_CHECKED recebido: ticketId={} eventId={}",
                event.getTicketId(), event.getEventId());
        eventService.registerCheckIn(event.getEventId());
    }
}
