package com.eda_project.tickets.repository;

import com.eda_project.tickets.model.TicketInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketInventoryRepository extends JpaRepository<TicketInventory, Long> {

    Optional<TicketInventory> findByEventId(Long eventId);
}
