package com.eda_project.tickets.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreatedEvent {

    private Long eventId;
    private String name;
    private String eventDate;
    private Integer maxCapacity;
    private BigDecimal basePrice;
    private String eventType;
    private String messageId;
    private String timestamp;
}
