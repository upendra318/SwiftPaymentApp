package com.SwiftPay.SwiftPayApp.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SwiftPay.SwiftPayApp.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByTransactionId(UUID transactionId);

}
