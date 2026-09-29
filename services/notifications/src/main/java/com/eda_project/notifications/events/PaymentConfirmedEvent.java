package com.eda_project.notifications.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentConfirmedEvent extends EventBase {

    private Long paymentId;
    private Long ticketId;
    private Long eventId;
    private String customerEmail;
    private BigDecimal amount;

    public PaymentConfirmedEvent(Long paymentId, Long ticketId, Long eventId, String customerEmail, BigDecimal amount) {
        super("PAYMENT_CONFIRMED");
        this.paymentId = paymentId;
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.customerEmail = customerEmail;
        this.amount = amount;
    }
}