package com.eda_project.payments.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PaymentsLogsService {

    public void logIncomingOrder(Long ticketId, Long eventId, BigDecimal amount, String messageId) {
        log.info("📩 [MENSAGERIA] Pedido recebido para processamento | Ticket ID: {} | Evento ID: {} | Valor: R$ {} | Msg ID: {}",
                ticketId, eventId, amount, messageId);
    }

    public void logIdempotencyHit(Long ticketId, String messageId) {
        log.warn("⚠️ [IDEMPOTÊNCIA] Pagamento duplicado detectado para ticketId={} ou messageId={}. Processamento ignorado.",
                ticketId, messageId);
    }

    public void logPaymentSuccess(Long paymentId, Long ticketId, BigDecimal amount) {
        log.info("✅ [PERSISTÊNCIA] Pagamento liquidado com sucesso | Payment ID: {} | Ticket ID: {} | Valor: R$ {}",
                paymentId, ticketId, amount);
    }

    public void logEventPublished(String exchange, Long paymentId, Long ticketId) {
        log.info("🚀 [BROADCAST] Evento PAYMENT_CONFIRMED publicado na exchange '{}' | Payment ID: {} | Ticket ID: {}",
                exchange, paymentId, ticketId);
    }

    public void logError(String action, String reason, Exception ex) {
        log.error("❌ [FALHA] Ação: {} | Motivo: {}", action, reason, ex);
    }
}