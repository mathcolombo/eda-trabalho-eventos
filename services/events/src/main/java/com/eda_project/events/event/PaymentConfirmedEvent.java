package com.eda_project.events.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {

    private Long paymentId;
    private Long ticketId;
    private Long eventId;
    private String status;
    private String eventType;
    private String messageId;
    private LocalDateTime timestamp;
}
