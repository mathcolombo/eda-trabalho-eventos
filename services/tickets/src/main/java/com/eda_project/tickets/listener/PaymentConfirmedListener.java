package com.eda_project.tickets.listener;

import com.eda_project.tickets.event.PaymentConfirmedEvent;
import com.eda_project.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmedListener {

    private final TicketService ticketService;

    @RabbitListener(queues = "${rabbitmq.queue.payment-confirmed}")
    public void handle(PaymentConfirmedEvent event) {
        log.info("PAYMENT_CONFIRMED recebido: ticketId={}", event.getTicketId());
        ticketService.confirmPayment(event);
    }
}
