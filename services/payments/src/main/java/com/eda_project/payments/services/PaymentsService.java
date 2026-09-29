package com.eda_project.payments.services;

import com.eda_project.payments.configs.RabbitMQConfig;
import com.eda_project.payments.events.PaymentConfirmedEvent;
import com.eda_project.payments.events.TicketPurchasedEvent;
import com.eda_project.payments.models.Payment;
import com.eda_project.payments.models.PaymentStatus;
import com.eda_project.payments.repositories.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PaymentsLogsService logService;

    @Transactional
    public void processPayment(TicketPurchasedEvent event) {
        logService.logIncomingOrder(
                event.getTicketId(),
                event.getEventId(),
                event.getAmount(),
                event.getMessageId()
        );

        // Verificação de Idempotência
        if (paymentRepository.existsByTicketId(event.getTicketId()) ||
                paymentRepository.existsByMessageId(event.getMessageId())) {
            logService.logIdempotencyHit(event.getTicketId(), event.getMessageId());
            return;
        }

        // Criar e persistir o registro com status PAID
        Payment payment = new Payment();
        payment.setTicketId(event.getTicketId());
        payment.setEventId(event.getEventId());
        payment.setCustomerName(event.getCustomerName());
        payment.setCustomerEmail(event.getCustomerEmail());
        payment.setAmount(event.getAmount());
        payment.setStatus(PaymentStatus.PAID);
        payment.setMessageId(event.getMessageId());
        payment.setProcessedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        logService.logPaymentSuccess(savedPayment.getId(), savedPayment.getTicketId(), savedPayment.getAmount());

        // Publicar evento PAYMENT_CONFIRMED no fanout
        PaymentConfirmedEvent confirmedEvent = new PaymentConfirmedEvent(
                savedPayment.getId(),
                savedPayment.getTicketId(),
                savedPayment.getEventId(),
                savedPayment.getStatus().name()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENTS_FANOUT,
                "", // chave vazia para exchange Fanout
                confirmedEvent
        );

        logService.logEventPublished(
                RabbitMQConfig.PAYMENTS_FANOUT,
                savedPayment.getId(),
                savedPayment.getTicketId()
        );
    }
}