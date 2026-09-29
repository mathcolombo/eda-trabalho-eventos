package com.eda_project.payments.listeners;

import com.eda_project.payments.configs.RabbitMQConfig;
import com.eda_project.payments.events.TicketPurchasedEvent;
import com.eda_project.payments.services.PaymentsLogsService;
import com.eda_project.payments.services.PaymentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentsEventListener {

    private final PaymentsService paymentService;
    private final PaymentsLogsService logService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_PROCESS)
    public void handleTicketPurchased(TicketPurchasedEvent event) {
        logService.logIncomingOrder(
                event.getTicketId(),
                event.getEventId(),
                event.getAmount(),
                event.getMessageId()
        );
        paymentService.processPayment(event);
    }
}