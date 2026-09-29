package com.eda_project.tickets.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String eventName;
    private Integer totalCapacity;
    private Integer reservedTickets;
    private Integer confirmedTickets;
    private Integer checkedInTickets;
    private BigDecimal basePrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
