package com.eda_project.tickets.controller;

import com.eda_project.tickets.model.Ticket;
import com.eda_project.tickets.model.TicketInventory;
import com.eda_project.tickets.repository.TicketInventoryRepository;
import com.eda_project.tickets.repository.TicketRepository;
import com.eda_project.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final TicketInventoryRepository inventoryRepository;

    @PostMapping("/purchase")
    public ResponseEntity<Ticket> purchase(@RequestBody PurchaseRequest request) {
        Ticket ticket = ticketService.purchase(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ticket);
    }

    @PostMapping("/{ticketId}/check-in")
    public ResponseEntity<Ticket> checkIn(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.checkIn(ticketId));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/inventory/{eventId}")
    public ResponseEntity<TicketInventory> getInventory(@PathVariable Long eventId) {
        return inventoryRepository.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
