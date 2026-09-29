package com.eda_project.notifications.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EventCreatedEvent extends EventBase {

    private Long eventId;
    private String name;
    private Integer totalCapacity;
    private BigDecimal price;
    private LocalDateTime eventDate;

    public EventCreatedEvent(Long eventId, String name, Integer totalCapacity, BigDecimal price, LocalDateTime eventDate) {
        super("EVENT_CREATED");
        this.eventId = eventId;
        this.name = name;
        this.totalCapacity = totalCapacity;
        this.price = price;
        this.eventDate = eventDate;
    }
}