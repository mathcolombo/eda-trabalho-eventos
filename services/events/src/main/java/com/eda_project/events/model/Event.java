package com.eda_project.events.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime eventDate;
    private Integer maxCapacity;
    private BigDecimal basePrice;
    private Integer confirmedTickets;
    private Integer checkedInTickets;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
