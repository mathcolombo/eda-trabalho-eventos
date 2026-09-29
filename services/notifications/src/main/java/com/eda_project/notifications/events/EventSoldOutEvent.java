package com.eda_project.notifications.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EventSoldOutEvent extends EventBase {

    private Long eventId;
    private String eventName;
    private Integer totalTicketsSold;

    public EventSoldOutEvent(Long eventId, String eventName, Integer totalTicketsSold) {
        super("EVENT_SOLDOUT");
        this.eventId = eventId;
        this.eventName = eventName;
        this.totalTicketsSold = totalTicketsSold;
    }
}