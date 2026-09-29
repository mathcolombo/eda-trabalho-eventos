package com.eda_project.events.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCheckedEvent {

    private Long ticketId;
    private Long eventId;
    private String eventType;
    private String messageId;
    private String timestamp;
}
