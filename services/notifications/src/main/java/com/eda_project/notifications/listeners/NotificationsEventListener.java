package com.eda_project.notifications.listeners;

import com.eda_project.notifications.configs.RabbitMQConfig;
import com.eda_project.notifications.events.EventCreatedEvent;
import com.eda_project.notifications.events.EventSoldOutEvent;
import com.eda_project.notifications.events.PaymentConfirmedEvent;
import com.eda_project.notifications.events.TicketCheckedEvent;
import com.eda_project.notifications.services.NotificationsLogsService;
import com.eda_project.notifications.services.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationsEventListener {

    private final NotificationsService notificationsService;
    private final NotificationsLogsService logService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIF_EVENTS)
    public void handleEventCreated(EventCreatedEvent event) {
        logService.logIncomingEvent("EVENT_CREATED", String.valueOf(event.getEventId()));
        notificationsService.notifyEventCreated(
                event.getEventId(),
                event.getName(),
                event.getTotalCapacity(),
                event.getPrice()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIF_PAYMENT)
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        logService.logIncomingEvent("PAYMENT_CONFIRMED", String.valueOf(event.getTicketId()));
        notificationsService.notifyPaymentConfirmed(
                event.getTicketId(),
                event.getEventId(),
                event.getCustomerEmail(),
                event.getAmount()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIF_CHECKIN)
    public void handleTicketChecked(TicketCheckedEvent event) {
        logService.logIncomingEvent("TICKET_CHECKED", String.valueOf(event.getTicketId()));
        notificationsService.notifyTicketChecked(
                event.getTicketId(),
                event.getEventId(),
                event.getCustomerEmail(),
                event.getGateNumber(),
                event.getCheckedAt()
        );
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIF_SOLDOUT)
    public void handleEventSoldOut(EventSoldOutEvent event) {
        logService.logIncomingEvent("EVENT_SOLDOUT", String.valueOf(event.getEventId()));
        notificationsService.notifyEventSoldOut(
                event.getEventId(),
                event.getEventName(),
                event.getTotalTicketsSold()
        );
    }
}