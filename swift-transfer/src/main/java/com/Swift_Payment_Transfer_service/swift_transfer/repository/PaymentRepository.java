package com.Swift_Payment_Transfer_service.swift_transfer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Swift_Payment_Transfer_service.swift_transfer.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByTransactionId(UUID transactionId);

    boolean existsByMessageId(String messageId);

}
