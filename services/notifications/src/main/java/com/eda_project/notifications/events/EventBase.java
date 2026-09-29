package com.eda_project.notifications.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class EventBase {

    private String messageId;
    private String eventType;
    private LocalDateTime timestamp;

    public EventBase(String eventType) {
        this.messageId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
}