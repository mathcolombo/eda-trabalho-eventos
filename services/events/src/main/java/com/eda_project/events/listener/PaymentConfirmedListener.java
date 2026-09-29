package com.eda_project.events.listener;

import com.eda_project.events.event.PaymentConfirmedEvent;
import com.eda_project.events.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmedListener {

    private final EventService eventService;

    @RabbitListener(queues = "events.payment-confirmed.queue")
    public void handle(PaymentConfirmedEvent event) {
        log.info("PAYMENT_CONFIRMED recebido: ticketId={} eventId={}",
                event.getTicketId(), event.getEventId());
        eventService.confirmTicket(event.getEventId());
    }
}
