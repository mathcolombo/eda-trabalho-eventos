package com.eda_project.tickets.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPurchasedEvent {

    private Long ticketId;
    private Long eventId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String eventType;
    private String messageId;
    private String timestamp;
}
