package com.eda_project.events.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public EventCreatedEvent(
            Long eventId,
            String name,
            LocalDateTime eventDate,
            Integer maxCapacity,
            BigDecimal basePrice) {
        this.eventId = eventId;
        this.name = name;
        this.eventDate = eventDate;
        this.totalCapacity = maxCapacity;
        this.price = basePrice;
        this.eventType = "EVENT_CREATED";
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }
}
