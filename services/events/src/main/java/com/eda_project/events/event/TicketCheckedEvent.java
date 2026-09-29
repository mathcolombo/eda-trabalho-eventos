package com.eda_project.events.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCheckedEvent {

    private Long ticketId;
    private Long eventId;
    private String customerEmail;
    private String gateNumber;
    private LocalDateTime checkedAt;
    private String eventType;
    private String messageId;
    private LocalDateTime timestamp;
}
