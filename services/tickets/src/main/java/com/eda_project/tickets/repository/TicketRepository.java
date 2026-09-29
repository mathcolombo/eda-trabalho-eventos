package com.eda_project.tickets.repository;

import com.eda_project.tickets.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
