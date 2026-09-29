package com.eda_project.payments.repositories;

import com.eda_project.payments.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payment, Long> {
    boolean existsByTicketId(Long ticketId);
    boolean existsByMessageId(String messageId);
    Optional<Payment> findByTicketId(Long ticketId);
}