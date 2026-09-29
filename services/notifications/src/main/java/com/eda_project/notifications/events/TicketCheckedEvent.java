package com.eda_project.notifications.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TicketCheckedEvent extends EventBase {

    private Long ticketId;
    private Long eventId;
    private String customerEmail;
    private String gateNumber;
    private LocalDateTime checkedAt;

    public TicketCheckedEvent(Long ticketId, Long eventId, String customerEmail, String gateNumber, LocalDateTime checkedAt) {
        super("TICKET_CHECKED");
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.customerEmail = customerEmail;
        this.gateNumber = gateNumber;
        this.checkedAt = checkedAt;
    }
}