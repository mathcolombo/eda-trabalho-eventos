package com.eda_project.payments.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentConfirmedEvent extends EventBase {

    private Long paymentId;
    private Long ticketId;
    private Long eventId;
    private String status;

    public PaymentConfirmedEvent(Long paymentId, Long ticketId, Long eventId, String status) {
        super("PAYMENT_CONFIRMED");
        this.paymentId = paymentId;
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.status = status;
    }
}