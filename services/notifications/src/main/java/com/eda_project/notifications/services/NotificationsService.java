package com.eda_project.notifications.services;

import com.eda_project.notifications.models.Notification;
import com.eda_project.notifications.models.NotificationChannel;
import com.eda_project.notifications.models.NotificationStatus;
import com.eda_project.notifications.models.NotificationType;
import com.eda_project.notifications.repositories.NotificationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationsService {

    private final NotificationsRepository notificationRepository;
    private final NotificationsLogsService logService;

    public void notifyEventCreated(Long eventId, String name, Integer totalCapacity, BigDecimal price) {
        String recipient = "all_users@platform.com";
        String title = "Novo Evento: " + name;
        String message = String.format("🎉 O evento '%s' foi criado! Capacidade: %d pessoas | Preço: R$ %.2f.",
                name, totalCapacity, price);

        saveAndLog(recipient, title, message, NotificationType.EVENT_CREATED,
                NotificationChannel.EMAIL, String.valueOf(eventId));
    }

    public void notifyPaymentConfirmed(Long ticketId, Long eventId, String customerEmail, BigDecimal amount) {
        String recipient = (customerEmail != null && !customerEmail.isBlank()) ? customerEmail : "cliente@platform.com";
        String title = "Pagamento Confirmado - Ingresso #" + ticketId;
        String message = String.format("✅ Pagamento de R$ %.2f aprovado para o ingresso #%d no evento #%d.",
                amount, ticketId, eventId);

        saveAndLog(recipient, title, message, NotificationType.PAYMENT_CONFIRMED,
                NotificationChannel.EMAIL, String.valueOf(ticketId));
    }

    public void notifyTicketChecked(Long ticketId, Long eventId, String customerEmail, String gateNumber, LocalDateTime checkedAt) {
        String recipient = (customerEmail != null && !customerEmail.isBlank()) ? customerEmail : "cliente@platform.com";
        String title = "Check-in Realizado";
        String message = String.format("🎟️ Acesso liberado no %s em %s para o ingresso #%d (Evento #%d).",
                gateNumber, checkedAt, ticketId, eventId);

        saveAndLog(recipient, title, message, NotificationType.TICKET_CHECKED,
                NotificationChannel.PUSH, String.valueOf(ticketId));
    }

    public void notifyEventSoldOut(Long eventId, String eventName, Integer totalTicketsSold) {
        String recipient = "admin@platform.com";
        String title = "Lotação Esgotada: " + eventName;
        String message = String.format("🚨 O evento '%s' (ID: %d) atingiu a capacidade máxima com %d ingressos vendidos.",
                eventName, eventId, totalTicketsSold);

        saveAndLog(recipient, title, message, NotificationType.EVENT_SOLD_OUT,
                NotificationChannel.EMAIL, String.valueOf(eventId));
    }

    private void saveAndLog(String recipient, String title, String message,
                            NotificationType type, NotificationChannel channel, String referenceId) {
        logService.logDispatch(channel.name(), recipient, message);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.SENT);
        notification.setReferenceId(referenceId);
        notification.setSentAt(LocalDateTime.now());
        notification.setSuccess(true);

        Notification saved = notificationRepository.save(notification);
        logService.logSuccess(saved.getId(), referenceId);
    }
}