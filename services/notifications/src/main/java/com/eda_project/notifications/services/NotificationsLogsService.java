package com.eda_project.notifications.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationsLogsService {

    public void logIncomingEvent(String eventType, String identifier) {
        log.info("📩 [MENSAGERIA] Evento recebido: {} | Identificador: {}", eventType, identifier);
    }

    public void logDispatch(String channel, String recipient, String message) {
        log.info("📢 [DISPARO] Canal: {} | Destinatário: {} | Conteúdo: {}", channel, recipient, message);
    }

    public void logSuccess(Long notificationId, String referenceId) {
        log.info("✅ [PERSISTÊNCIA] Notificação registrada no banco. ID: {} | Ref: {}", notificationId, referenceId);
    }

    public void logError(String action, String reason, Exception ex) {
        log.error("❌ [FALHA] Ação: {} | Motivo: {}", action, reason, ex);
    }
}