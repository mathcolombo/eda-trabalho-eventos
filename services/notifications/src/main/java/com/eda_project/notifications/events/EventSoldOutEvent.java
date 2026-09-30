package com.eda_project.notifications.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
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