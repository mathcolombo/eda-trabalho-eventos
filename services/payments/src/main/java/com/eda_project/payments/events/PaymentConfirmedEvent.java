package com.eda_project.payments.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentConfirmedEvent extends EventBase {

    private Long paymentId;
    private Long ticketId;
    private Long eventId;
    private String customerEmail;
    private BigDecimal amount;
    private String status;

    public PaymentConfirmedEvent(Long paymentId, Long ticketId, Long eventId,
                                 String customerEmail, BigDecimal amount, String status) {
        super("PAYMENT_CONFIRMED");
        this.paymentId = paymentId;
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.status = status;
    }
}