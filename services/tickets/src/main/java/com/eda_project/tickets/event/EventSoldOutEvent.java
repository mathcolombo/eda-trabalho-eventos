package com.eda_project.tickets.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSoldOutEvent {

    private Long eventId;
    private String eventName;
    private Integer totalTicketsSold;
    private String eventType;
    private String messageId;
    private LocalDateTime timestamp;
}
