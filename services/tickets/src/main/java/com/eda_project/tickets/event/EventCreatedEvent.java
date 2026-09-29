package com.eda_project.tickets.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreatedEvent {

    private Long eventId;
    private String name;
    private LocalDateTime eventDate;
    private Integer totalCapacity;
    private BigDecimal price;
    private String eventType;
    private String messageId;
    private LocalDateTime timestamp;
}
