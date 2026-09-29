package com.eda_project.payments.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TicketPurchasedEvent extends EventBase {

    private Long ticketId;
    private Long eventId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
}